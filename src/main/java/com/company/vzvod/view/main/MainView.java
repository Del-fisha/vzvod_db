package com.company.vzvod.view.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.company.vzvod.entity.User;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
@ViewController(id = "MainView")
@ViewDescriptor(path = "main-view.xml")
public class MainView extends StandardMainView {

    private static final String AUDIO_ID = "bg-audio";
    private static final String HOVER_SFX_ID = "ui-hover-sfx";
    private static final String CLICK_SFX_ID = "ui-click-sfx";

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Subscribe
    public void onInit(final InitEvent event) {
        BgAudio audio = buildAudio();
        attachToContent(audio);

        BgAudio hoverSfx = buildSfxAudio(HOVER_SFX_ID, "/audio/button.mp3", 0.3);
        BgAudio clickSfx = buildSfxAudio(CLICK_SFX_ID, "/audio/push.mp3", 0.5);
        attachToContent(hoverSfx);
        attachToContent(clickSfx);

        String storageKeyPrefix = getStorageKeyPrefix();

        VolumeSlider volumeSlider = new VolumeSlider(audio.getVolume());
        Button muteButton = new Button();
        Component controls = buildControls(storageKeyPrefix, audio, volumeSlider, muteButton);
        attachToContent(controls);

        applySavedSettings(storageKeyPrefix, audio, volumeSlider, muteButton);
        installUiSoundEffects();

        // Autoplay may still be blocked by browser policies; best-effort start.
        UI.getCurrent().getPage().executeJs(
                "const a=document.getElementById($0); if(a){a.play().catch(()=>{});} ",
                AUDIO_ID
        );
    }

    private BgAudio buildAudio() {
        BgAudio audio = new BgAudio();
        audio.setId(AUDIO_ID);
        audio.setSrc("/audio/theme.mp3");
        audio.setAutoplay(true);
        audio.setLoop(true);
        audio.setVolume(0.5);
        audio.getStyle().set("display", "none");
        audio.getElement().setAttribute("preload", "auto");
        return audio;
    }

    private BgAudio buildSfxAudio(String id, String src, double volume) {
        BgAudio audio = new BgAudio();
        audio.setId(id);
        audio.setSrc(src);
        audio.setAutoplay(false);
        audio.setLoop(false);
        audio.setMuted(false);
        audio.setVolume(volume);
        audio.getStyle().set("display", "none");
        audio.getElement().setAttribute("preload", "auto");
        return audio;
    }

    private Component buildControls(String storageKeyPrefix, BgAudio audio, VolumeSlider volume, Button mute) {
        volume.setWidth("160px");
        volume.addInputListener(v -> {
            audio.setVolume(v);
            saveVolume(storageKeyPrefix, audio.getVolume());
        });

        mute.setText(audio.isMuted() ? "Unmute" : "Mute");
        mute.addClickListener(e -> {
            audio.setMuted(!audio.isMuted());
            mute.setText(audio.isMuted() ? "Unmute" : "Mute");
            saveMuted(storageKeyPrefix, audio.isMuted());
        });

        HorizontalLayout box = new HorizontalLayout(volume, mute);
        box.addClassName("bg-audio-controls");
        box.setPadding(false);
        box.setSpacing(true);
        return box;
    }

    private void attachToContent(Component component) {
        Component content = getContent();
        if (content instanceof HasComponents hasComponents) {
            hasComponents.add(component);
        } else {
            content.getElement().appendChild(component.getElement());
        }
    }

    private String getStorageKeyPrefix() {
        try {
            Object u = currentAuthentication.getUser();
            if (u instanceof User user && user.getUsername() != null && !user.getUsername().isBlank()) {
                return "vzvod.bgAudio." + user.getUsername();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "vzvod.bgAudio";
    }

    private void applySavedSettings(String storageKeyPrefix, BgAudio audio, VolumeSlider volume, Button mute) {
        UI.getCurrent().getPage()
                .executeJs("return localStorage.getItem($0)", storageKeyPrefix + ".volume")
                .then(String.class, raw -> {
                    if (raw == null || raw.isBlank()) {
                        return;
                    }
                    try {
                        double v = Double.parseDouble(raw);
                        audio.setVolume(v);
                        volume.setValue(audio.getVolume());
                    } catch (NumberFormatException ignored) {
                        // ignore invalid value
                    }
                });

        UI.getCurrent().getPage()
                .executeJs("return localStorage.getItem($0)", storageKeyPrefix + ".muted")
                .then(String.class, raw -> {
                    if (raw == null || raw.isBlank()) {
                        return;
                    }
                    boolean muted = "true".equalsIgnoreCase(raw);
                    audio.setMuted(muted);
                    mute.setText(audio.isMuted() ? "Unmute" : "Mute");
                });
    }

    private void saveVolume(String storageKeyPrefix, double volume) {
        UI.getCurrent().getPage().executeJs(
                "localStorage.setItem($0, String($1))",
                storageKeyPrefix + ".volume",
                volume
        );
    }

    private void installUiSoundEffects() {
        UI.getCurrent().getPage().executeJs(
                """
                (function(){
                  if (window.__vzvodUiSfxInstalled) return;
                  window.__vzvodUiSfxInstalled = true;

                  const hoverId = $0;
                  const clickId = $1;

                  function isInteractive(el){
                    if (!el) return false;
                    const t = el.closest('button,a,[role="button"],input,textarea,select,vaadin-button,vaadin-tab,vaadin-item,[tabindex]');
                    if (!t) return false;
                    if (t.hasAttribute('disabled')) return false;
                    if (t.getAttribute('aria-disabled') === 'true') return false;
                    return true;
                  }

                  function play(id){
                    const a = document.getElementById(id);
                    if (!a) return;
                    try {
                      a.currentTime = 0;
                      const p = a.play();
                      if (p && p.catch) p.catch(()=>{});
                    } catch(e) {}
                  }

                  let lastHoverAt = 0;
                  document.addEventListener('pointerover', function(ev){
                    const now = Date.now();
                    if (now - lastHoverAt < 120) return;
                    const el = ev.target;
                    if (!isInteractive(el)) return;
                    lastHoverAt = now;
                    play(hoverId);
                  }, {capture:true, passive:true});

                  document.addEventListener('click', function(ev){
                    const el = ev.target;
                    if (!isInteractive(el)) return;
                    play(clickId);
                  }, {capture:true, passive:true});
                })();
                """,
                HOVER_SFX_ID,
                CLICK_SFX_ID
        );
    }

    private void saveMuted(String storageKeyPrefix, boolean muted) {
        UI.getCurrent().getPage().executeJs(
                "localStorage.setItem($0, String($1))",
                storageKeyPrefix + ".muted",
                muted
        );
    }

    @Tag("input")
    private static final class VolumeSlider extends HtmlComponent {
        public VolumeSlider(double initialVolume) {
            getElement().setAttribute("type", "range");
            getElement().setAttribute("min", "0");
            getElement().setAttribute("max", "1");
            getElement().setAttribute("step", "0.01");
            setValue(initialVolume);
            addClassName("bg-volume-slider");
        }

        public void setValue(double volume) {
            double v = Math.max(0, Math.min(1, volume));
            getElement().setProperty("value", String.valueOf(v));
        }

        public void addInputListener(java.util.function.DoubleConsumer listener) {
            getElement()
                    .addEventListener("input", e -> {
                        String raw = e.getEventData().getString("event.target.value");
                        try {
                            listener.accept(Double.parseDouble(raw));
                        } catch (NumberFormatException ignored) {
                            // ignore invalid value
                        }
                    })
                    .addEventData("event.target.value");
        }
    }

    @Tag("audio")
    private static final class BgAudio extends HtmlComponent {
        private boolean muted;
        private double volume = 0.5;

        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        public void setAutoplay(boolean autoplay) {
            getElement().setProperty("autoplay", autoplay);
            if (autoplay) {
                getElement().setAttribute("autoplay", "");
            } else {
                getElement().removeAttribute("autoplay");
            }
        }

        public void setLoop(boolean loop) {
            getElement().setProperty("loop", loop);
            if (loop) {
                getElement().setAttribute("loop", "");
            } else {
                getElement().removeAttribute("loop");
            }
        }

        public void setMuted(boolean muted) {
            this.muted = muted;
            getElement().setProperty("muted", muted);
        }

        public boolean isMuted() {
            return muted;
        }

        public void setVolume(double volume) {
            double v = Math.max(0, Math.min(1, volume));
            this.volume = v;
            getElement().setProperty("volume", v);
        }

        public double getVolume() {
            return volume;
        }
    }
}
