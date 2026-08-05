import {LitElement} from 'lit';

const POLL_INTERVAL_MS = 5000;
const DOC_EXTENSIONS = ['.doc', '.docx'];
const STORAGE_DB = 'vzvod-orientations';
const STORAGE_STORE = 'handles';
const HANDLE_KEY = 'folder';
const PICKER_ID = 'vzvod-orientations';

class OrientationsClient extends LitElement {

    createRenderRoot() {
        return this;
    }

    constructor() {
        super();
        this._dirHandle = null;
        this._pollTimer = null;
        this._lastScanSignature = null;
        this._restoreStarted = false;
    }

    connectedCallback() {
        super.connectedCallback();
        this.tryRestoreSavedFolder();
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        this.stopMonitoring();
    }

    async tryRestoreSavedFolder() {
        if (this._restoreStarted || this._dirHandle || !window.showDirectoryPicker) {
            return;
        }
        this._restoreStarted = true;
        try {
            const handle = await this._loadDirectoryHandle();
            if (!handle) {
                return;
            }
            if (!await this._ensurePermission(handle)) {
                return;
            }
            this._dirHandle = handle;
            await this._scanCurrentFolder();
            this.startMonitoring();
        } catch (error) {
            // Ignore restore errors; user can pick the folder manually.
        }
    }

    async openFolderPicker() {
        if (!window.showDirectoryPicker) {
            this._notifyError('Браузер не поддерживает выбор папки. Используйте Chrome или Edge.');
            return;
        }
        try {
            this._dirHandle = await window.showDirectoryPicker({
                mode: 'read',
                id: PICKER_ID
            });
            await this._saveDirectoryHandle(this._dirHandle);
            this._lastScanSignature = null;
            await this._scanCurrentFolder();
            this.startMonitoring();
        } catch (err) {
            if (err && err.name !== 'AbortError') {
                this._notifyError(err.message || String(err));
            }
        }
    }

    startMonitoring() {
        if (this._pollTimer) {
            clearInterval(this._pollTimer);
        }
        this._pollTimer = setInterval(() => {
            this._scanCurrentFolder().catch(error => this._notifyError(error.message || String(error)));
        }, POLL_INTERVAL_MS);
    }

    stopMonitoring() {
        if (this._pollTimer) {
            clearInterval(this._pollTimer);
            this._pollTimer = null;
        }
    }

    async rescanFolder() {
        if (!this._dirHandle) {
            this._notifyError('Сначала выберите папку.');
            return;
        }
        this._lastScanSignature = null;
        await this._scanCurrentFolder();
    }

    async _scanCurrentFolder() {
        if (!this._dirHandle) {
            return;
        }
        const files = [];
        const signatureParts = [];
        for await (const entry of this._dirHandle.values()) {
            if (entry.kind !== 'file') {
                continue;
            }
            const lowerName = entry.name.toLowerCase();
            if (!DOC_EXTENSIONS.some(ext => lowerName.endsWith(ext))) {
                continue;
            }
            const file = await entry.getFile();
            signatureParts.push(`${entry.name}:${file.lastModified}:${file.size}`);
            files.push({
                fileName: entry.name,
                contentBase64: await this._fileToBase64(file)
            });
        }

        const signature = signatureParts.sort().join('\n');
        if (signature === this._lastScanSignature) {
            return;
        }
        this._lastScanSignature = signature;

        this.$server.onFilesPayload(JSON.stringify({files}));
    }

    async _fileToBase64(file) {
        const buffer = await file.arrayBuffer();
        const bytes = new Uint8Array(buffer);
        let binary = '';
        const chunkSize = 0x8000;
        for (let i = 0; i < bytes.length; i += chunkSize) {
            binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunkSize));
        }
        return btoa(binary);
    }

    async copyToClipboard(text, imagesJson) {
        const plainText = text || '';
        const images = this._parseImages(imagesJson);
        const html = this._buildClipboardHtml(plainText, images);

        try {
            if (navigator.clipboard && window.ClipboardItem) {
                await navigator.clipboard.write([
                    new ClipboardItem({
                        'text/plain': new Blob([plainText], {type: 'text/plain;charset=utf-8'}),
                        'text/html': new Blob([html], {type: 'text/html;charset=utf-8'})
                    })
                ]);
                this.$server.onCopied();
                return;
            }
        } catch (error) {
            // Fall through to execCommand below.
        }

        try {
            this._copyRichHtml(html, plainText);
            this.$server.onCopied();
        } catch (error) {
            this._notifyError(error.message || String(error));
        }
    }

    async _ensurePermission(handle) {
        const options = {mode: 'read'};
        if ((await handle.queryPermission(options)) === 'granted') {
            return true;
        }
        return (await handle.requestPermission(options)) === 'granted';
    }

    _openDb() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(STORAGE_DB, 1);
            request.onupgradeneeded = () => {
                if (!request.result.objectStoreNames.contains(STORAGE_STORE)) {
                    request.result.createObjectStore(STORAGE_STORE);
                }
            };
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async _saveDirectoryHandle(handle) {
        const db = await this._openDb();
        await new Promise((resolve, reject) => {
            const tx = db.transaction(STORAGE_STORE, 'readwrite');
            tx.objectStore(STORAGE_STORE).put(handle, HANDLE_KEY);
            tx.oncomplete = () => resolve();
            tx.onerror = () => reject(tx.error);
        });
    }

    async _loadDirectoryHandle() {
        const db = await this._openDb();
        return new Promise((resolve, reject) => {
            const tx = db.transaction(STORAGE_STORE, 'readonly');
            const request = tx.objectStore(STORAGE_STORE).get(HANDLE_KEY);
            request.onsuccess = () => resolve(request.result || null);
            request.onerror = () => reject(request.error);
        });
    }

    _parseImages(imagesJson) {
        if (!imagesJson) {
            return [];
        }
        try {
            const parsed = JSON.parse(imagesJson);
            return Array.isArray(parsed) ? parsed : [];
        } catch (error) {
            return [];
        }
    }

    _buildClipboardHtml(text, images) {
        const escaped = this._escapeHtml(text).replace(/\r?\n/g, '<br>');
        const imageHtml = (images || [])
            .filter(image => image && image.base64)
            .map(image => {
                const mimeType = image.contentType || 'image/png';
                return `<img src="data:${mimeType};base64,${image.base64}" />`;
            })
            .join('<br>');
        return `<!DOCTYPE html><html><body><div>${escaped}</div><br>${imageHtml}</body></html>`;
    }

    _copyRichHtml(html, plainText) {
        const container = document.createElement('div');
        container.contentEditable = 'true';
        container.style.position = 'fixed';
        container.style.left = '-9999px';
        container.innerHTML = html;
        document.body.appendChild(container);

        const selection = window.getSelection();
        const range = document.createRange();
        range.selectNodeContents(container);
        selection.removeAllRanges();
        selection.addRange(range);

        const copied = document.execCommand('copy');
        document.body.removeChild(container);
        selection.removeAllRanges();

        if (!copied) {
            throw new Error('Не удалось скопировать текст и изображение.');
        }

        if (plainText && navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(plainText).catch(() => {});
        }
    }

    _escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    _notifyError(message) {
        if (this.$server && this.$server.onClientError) {
            this.$server.onClientError(message);
        }
    }
}

customElements.define('orientations-client', OrientationsClient);
