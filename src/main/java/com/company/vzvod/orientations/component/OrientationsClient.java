package com.company.vzvod.orientations.component;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.function.Consumer;

@Tag("orientations-client")
@JsModule("./orientations/orientations-client.js")
public class OrientationsClient extends Component {

    private Consumer<String> filesPayloadListener;
    private Runnable copiedListener;
    private Consumer<String> clientErrorListener;

    public void openFolderPicker() {
        getElement().callJsFunction("openFolderPicker");
    }

    public void tryRestoreSavedFolder() {
        getElement().callJsFunction("tryRestoreSavedFolder");
    }

    public void rescanFolder() {
        getElement().callJsFunction("rescanFolder");
    }

    public void copyToClipboard(String text, String imagesJson) {
        getElement().callJsFunction("copyToClipboard", text, imagesJson);
    }

    public void setFilesPayloadListener(Consumer<String> listener) {
        this.filesPayloadListener = listener;
    }

    public void setCopiedListener(Runnable listener) {
        this.copiedListener = listener;
    }

    public void setClientErrorListener(Consumer<String> listener) {
        this.clientErrorListener = listener;
    }

    @ClientCallable
    public void onFilesPayload(String jsonPayload) {
        if (filesPayloadListener != null) {
            filesPayloadListener.accept(jsonPayload);
        }
    }

    @ClientCallable
    public void onCopied() {
        if (copiedListener != null) {
            copiedListener.run();
        }
    }

    @ClientCallable
    public void onClientError(String message) {
        if (clientErrorListener != null) {
            clientErrorListener.accept(message);
        }
    }
}
