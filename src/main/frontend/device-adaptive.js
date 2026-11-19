/**
 * Определяет экран устройства (телефон / планшет / монитор) и подгоняет
 * рабочие экраны под видимую область. Стартовый дашборд прокручивается сам.
 */
const PHONE = 'device-phone';
const TABLET = 'device-tablet';
const DESKTOP = 'device-desktop';
const PORTRAIT = 'orientation-portrait';
const LANDSCAPE = 'orientation-landscape';

function classify() {
    const shortSide = Math.min(window.screen.width, window.screen.height);
    const longSide = Math.max(window.screen.width, window.screen.height);
    const coarse = window.matchMedia('(pointer: coarse)').matches || navigator.maxTouchPoints > 1;
    const fine = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
    const viewportShort = Math.min(window.innerWidth, window.innerHeight);

    let device = DESKTOP;
    if (shortSide <= 520 && (!fine || coarse)) {
        device = PHONE;
    } else if (coarse && shortSide <= 1200 && longSide <= 1600) {
        device = TABLET;
    }
    if (device === DESKTOP && coarse && !fine && viewportShort <= 520) {
        device = PHONE;
    } else if (device === DESKTOP && coarse && !fine && viewportShort <= 1100 && longSide <= 1600) {
        device = TABLET;
    }

    const orientation = window.innerWidth >= window.innerHeight ? LANDSCAPE : PORTRAIT;
    const root = document.documentElement;
    root.classList.remove(PHONE, TABLET, DESKTOP, PORTRAIT, LANDSCAPE);
    root.classList.add(device, orientation);
    root.dataset.device = device === PHONE ? 'phone' : device === TABLET ? 'tablet' : 'desktop';
    return device;
}

function contentBox() {
    const layout = document.querySelector('vaadin-app-layout');
    const part = layout && layout.shadowRoot && layout.shadowRoot.querySelector('[part="content"]');
    if (part && part.clientHeight > 40) {
        return part;
    }
    return null;
}

function viewRoots() {
    const layout = document.querySelector('vaadin-app-layout');
    if (!layout) {
        return [];
    }
    return [...layout.children].filter((el) => {
        const slot = el.getAttribute('slot');
        return !slot || slot === 'content';
    });
}

function isStartDashboard(el) {
    return el.classList.contains('home-stats-entry')
        || !!el.querySelector(':scope > .home-stats-entry, .home-stats-widget-slot');
}

function isTodayShiftDashboard(el) {
    return el.classList.contains('today-shift-dashboard')
        || !!el.querySelector(':scope .today-shift-dashboard');
}

function hasInternalGridScroll(el) {
    return !!el.querySelector('vaadin-grid:not([all-rows-visible])');
}

function stackLabels(device) {
    const phone = device === PHONE;
    document.querySelectorAll('vaadin-form-layout').forEach((layout) => {
        if (!('labelsAside' in layout) && !layout.hasAttribute('labels-aside')) {
            return;
        }
        if (phone) {
            layout.labelsAside = false;
            layout.removeAttribute('labels-aside');
        }
    });
}

function pinDrawer(device, orientation) {
    const layout = document.querySelector('vaadin-app-layout');
    if (!layout || !('overlay' in layout)) {
        return;
    }
    if (device === DESKTOP) {
        return;
    }
    layout.overlay = device === PHONE || orientation === PORTRAIT;
}

function applyZoom(el, availW, availH, minZoom) {
    const previousZoom = el.style.zoom;
    const previousHeight = el.style.height;
    const previousMaxHeight = el.style.maxHeight;
    const previousOverflow = el.style.overflow;

    el.style.zoom = '1';
    el.style.height = 'auto';
    el.style.maxHeight = 'none';
    el.style.overflow = 'visible';

    const needH = el.scrollHeight;
    const needW = el.scrollWidth;
    let zoom = 1;
    if (needH > availH + 6 && needH > 0) {
        zoom = Math.min(zoom, availH / needH);
    }
    if (needW > availW + 6 && needW > 0) {
        zoom = Math.min(zoom, availW / needW);
    }

    if (zoom > 0.97) {
        el.style.zoom = '';
        el.style.height = '100%';
        el.style.maxHeight = '100%';
        el.style.overflow = 'hidden';
        return;
    }

    const clipped = zoom < minZoom;
    zoom = Math.max(zoom, minZoom);
    const nextZoom = zoom.toFixed(3);
    if (el.dataset.fitZoom === nextZoom && !clipped) {
        el.style.zoom = previousZoom;
        el.style.height = previousHeight;
        el.style.maxHeight = previousMaxHeight;
        el.style.overflow = previousOverflow;
        return;
    }
    el.dataset.fitZoom = nextZoom;
    el.style.zoom = nextZoom;
    el.style.height = 'auto';
    el.style.maxHeight = clipped ? Math.floor(availH / zoom) + 'px' : 'none';
    el.style.overflow = clipped ? 'auto' : 'hidden';
}

function lockToViewport(el) {
    el.dataset.fitZoom = '';
    el.style.zoom = '';
    el.style.height = '100%';
    el.style.maxHeight = '100%';
    el.style.overflow = 'hidden';
    el.style.minHeight = '0';
    el.style.maxWidth = '100%';
}

function release(el) {
    el.dataset.fitZoom = '';
    el.style.zoom = '';
    el.style.height = '';
    el.style.maxHeight = '';
    el.style.overflow = '';
    el.style.minHeight = '';
    el.style.maxWidth = '';
}

function fitViews(device) {
    const box = contentBox();
    const roots = viewRoots();
    if (!box) {
        return;
    }
    const minZoom = device === PHONE ? 0.5 : 0.68;
    roots.forEach((el) => {
        if (device === DESKTOP) {
            release(el);
            return;
        }
        if (isStartDashboard(el)) {
            release(el);
            el.style.height = '100%';
            el.style.maxHeight = '100%';
            el.style.overflow = 'auto';
            return;
        }
        if (isTodayShiftDashboard(el) || hasInternalGridScroll(el)) {
            lockToViewport(el);
            return;
        }
        watchGrowingView(el);
        applyZoom(el, box.clientWidth, box.clientHeight, minZoom);
    });
}

function fitDialogs(device) {
    if (device === DESKTOP) {
        return;
    }
    const minZoom = device === PHONE ? 0.55 : 0.7;
    document.querySelectorAll('vaadin-dialog-overlay').forEach((overlay) => {
        const content = overlay.shadowRoot && overlay.shadowRoot.querySelector('[part="content"]');
        const inner = content && content.firstElementChild;
        if (!inner) {
            return;
        }
        const availW = Math.min(window.innerWidth, overlay.clientWidth || window.innerWidth) - 8;
        const availH = window.innerHeight - 8;
        applyZoom(inner, availW, availH, minZoom);
    });
}

let scheduled = 0;
let applying = false;
const growingViews = new WeakSet();
const growingObserver = new MutationObserver(() => {
    if (!applying) {
        schedule();
    }
});

function watchGrowingView(el) {
    if (growingViews.has(el)) {
        return;
    }
    growingViews.add(el);
    growingObserver.observe(el, {childList: true, subtree: true});
}

function apply() {
    if (applying) {
        return;
    }
    applying = true;
    try {
        const device = classify();
        const orientation = document.documentElement.classList.contains(PORTRAIT) ? PORTRAIT : LANDSCAPE;
        pinDrawer(device, orientation);
        stackLabels(device);
        fitViews(device);
        fitDialogs(device);
    } finally {
        requestAnimationFrame(() => {
            applying = false;
        });
    }
}

function schedule() {
    window.clearTimeout(scheduled);
    scheduled = window.setTimeout(apply, 60);
}

function install() {
    if (window.__vzvodDeviceAdaptive) {
        return;
    }
    window.__vzvodDeviceAdaptive = true;
    apply();
    window.addEventListener('resize', schedule, {passive: true});
    window.addEventListener('orientationchange', schedule);
    window.addEventListener('vaadin-navigated', schedule);
    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', schedule);
    }
    const observer = new MutationObserver(schedule);
    observer.observe(document.body, {childList: true, subtree: false});
    const layout = document.querySelector('vaadin-app-layout');
    if (layout) {
        observer.observe(layout, {childList: true, subtree: false});
    } else {
        const wait = new MutationObserver(() => {
            const found = document.querySelector('vaadin-app-layout');
            if (found) {
                observer.observe(found, {childList: true, subtree: false});
                wait.disconnect();
                schedule();
            }
        });
        wait.observe(document.body, {childList: true, subtree: true});
    }
}

install();
