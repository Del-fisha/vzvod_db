package com.company.vzvod.view.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import com.company.vzvod.entity.User;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.MessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import com.company.vzvod.notification.UserNotificationService;
import com.company.vzvod.notification.UserNotificationService.StoredOverduePayload;
import com.company.vzvod.notification.UserNotificationKind;
import com.company.vzvod.notification.OverdueItemDto;
import com.company.vzvod.notification.OverdueItemType;
import com.company.vzvod.entity.UserNotification;
import com.company.vzvod.view.dashboard.WorkResultsStatisticsDialog;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.view.DialogWindow;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Route("")
@ViewController(id = "MainView")
@ViewDescriptor(path = "main-view.xml")
public class MainView extends StandardMainView {

    private static final String AUDIO_ID = "bg-audio";
    private static final String HOVER_SFX_ID = "ui-hover-sfx";
    private static final String CLICK_SFX_ID = "ui-click-sfx";

    @Autowired
    private CurrentAuthentication currentAuthentication;

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private DialogWindows dialogWindows;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private VerticalLayout homeStatsWidgetSlot;

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
        installHomeStatsWidget();

        // Autoplay may still be blocked by browser policies; best-effort start.
        UI.getCurrent().getPage().executeJs(
                "const a=document.getElementById($0); if(a){a.play().catch(()=>{});} ",
                AUDIO_ID
        );

        showLoginNotifications();
    }

    private void installHomeStatsWidget() {
        if (homeStatsWidgetSlot == null) {
            return;
        }
        homeStatsWidgetSlot.removeAll();

        String title = messageBundle.getMessage("openWorkResultsStatsBtn.text");
        String subtitle = "Откройте сводную статистику по работе за выбранный период.";
        String cta = "Открыть";

        HomeStatsCard card = new HomeStatsCard(title, subtitle, cta, "var(--lumo-primary-color)");
        card.addCardClickListener(() -> {
            DialogWindow<WorkResultsStatisticsDialog> w = dialogWindows.view(this, WorkResultsStatisticsDialog.class).build();
            w.open();
        });
        homeStatsWidgetSlot.add(card);
    }

    @Tag("article")
    private static final class HomeStatsCard extends HtmlComponent {
        private Runnable onClick;

        HomeStatsCard(String title, String subtitle, String cta, String color) {
            addClassName("home-card");
            getElement().getStyle().set("--clr", (color == null || color.isBlank()) ? "var(--lumo-primary-color)" : color);

            getElement().setProperty("innerHTML", buildInnerHtml(
                    escapeHtml(title),
                    escapeHtml(subtitle),
                    escapeHtml(cta)
            ));

            getElement().addEventListener("click", e -> {
                if (onClick != null) {
                    onClick.run();
                }
            });
            getElement().getStyle().set("cursor", "pointer");
        }

        void addCardClickListener(Runnable r) {
            this.onClick = r;
        }

        private static String buildInnerHtml(String title, String subtitle, String cta) {
            return """
                    <div class="home-card__icon" aria-hidden="true">📊</div>
                    <h3 class="home-card__title">%s</h3>
                    <div class="home-card__subtitle">%s</div>
                    <div class="home-card__cta" role="button" tabindex="0">%s</div>
                    """.formatted(title, subtitle, cta);
        }

        private static String escapeHtml(String s) {
            if (s == null) {
                return "";
            }
            return s
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
        }
    }

    private void showLoginNotifications() {
        UUID userId = currentUserIdOrNull();
        if (userId == null) {
            return;
        }
        List<UserNotification> active = userNotificationService.loadActiveForUser(userId);
        if (active.isEmpty()) {
            return;
        }
        active = deduplicateForDisplay(active);
        if (active.isEmpty()) {
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setResizable(true);
        dialog.setWidth("min(900px, 95vw)");
        dialog.setHeight("min(85vh, 900px)");

        H3 title = new H3(messageBundle.getMessage("notification.dialog.title"));

        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(true);
        body.setWidthFull();

        for (UserNotification n : active) {
            body.add(renderNotificationBlock(body, n, userId, dialog));
        }

        Scroller scroller = new Scroller(body);
        scroller.setSizeFull();

        JmixButton remindLater = new JmixButton();
        remindLater.setText(messageBundle.getMessage("notification.dialog.remindLater"));
        remindLater.addClickListener(e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(remindLater);
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout root = new VerticalLayout(title, scroller, actions);
        root.setPadding(true);
        root.setSpacing(true);
        root.setSizeFull();
        root.expand(scroller);
        dialog.add(root);

        dialog.open();
    }

    private List<UserNotification> deduplicateForDisplay(List<UserNotification> active) {
        if (active == null || active.isEmpty()) {
            return List.of();
        }
        // active is ordered by createdAt desc, so "first wins"
        Map<String, UserNotification> byKey = new LinkedHashMap<>();
        for (UserNotification n : active) {
            String key = buildDedupKey(n);
            byKey.putIfAbsent(key, n);
        }
        return new ArrayList<>(byKey.values());
    }

    private String buildDedupKey(UserNotification n) {
        if (n == null) {
            return "null";
        }
        if (!UserNotificationKind.OVERDUE.equals(n.getKind()) || n.getPayload() == null) {
            return n.getKind() + ":" + n.getId();
        }
        try {
            StoredOverduePayload payload = objectMapper.readValue(n.getPayload(), StoredOverduePayload.class);
            UUID subjectId = payload == null ? null : payload.subjectUserId();
            return n.getKind() + ":" + subjectId;
        } catch (Exception e) {
            return n.getKind() + ":" + n.getId();
        }
    }

    private UUID currentUserIdOrNull() {
        try {
            Object u = currentAuthentication.getUser();
            if (u instanceof User user) {
                return user.getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Component renderNotificationBlock(VerticalLayout container, UserNotification n, UUID userId, Dialog dialog) {
        VerticalLayout box = new VerticalLayout();
        box.setPadding(true);
        box.setSpacing(false);
        box.setWidthFull();
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");

        String text = safeRenderOverdue(n);
        Pre pre = new Pre(text);
        pre.getStyle().set("white-space", "pre-wrap");
        pre.getStyle()
                .set("margin", "0")
                .set("line-height", "1.25");

        JmixButton fixed = new JmixButton();
        fixed.setText(messageBundle.getMessage("notification.dialog.fixed"));
        fixed.addClickListener(e -> {
            userNotificationService.resolve(n.getId(), userId);
            if (container != null) {
                container.remove(box);
                if (container.getComponentCount() == 0) {
                    dialog.close();
                }
            } else {
                dialog.close();
            }
        });

        HorizontalLayout footer = new HorizontalLayout(fixed);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        box.add(pre, footer);
        return box;
    }

    private String safeRenderOverdue(UserNotification n) {
        if (n == null || n.getPayload() == null) {
            return "";
        }
        try {
            StoredOverduePayload payload = objectMapper.readValue(n.getPayload(), StoredOverduePayload.class);
            return renderOverduePayload(payload);
        } catch (Exception e) {
            return n.getPayload();
        }
    }

    private String renderOverduePayload(StoredOverduePayload payload) {
        UUID currentUserId = currentUserIdOrNull();
        if (payload == null || payload.items() == null || payload.items().isEmpty()) {
            return "";
        }

        boolean isSubject = currentUserId != null && currentUserId.equals(payload.subjectUserId());
        StringBuilder sb = new StringBuilder();

        if (isSubject) {
            sb.append(messageBundle.getMessage("notification.overdue.subject.who")).append('\n');
        } else {
            String fio = "";
            try {
                User subject = dataManager.load(User.class).id(payload.subjectUserId()).one();
                fio = subject.getShortFio();
            } catch (Exception ignored) {
                // keep empty
            }
            String whoTemplate = messageBundle.getMessage("notification.overdue.commander.who");
            sb.append(String.format(Locale.getDefault(), whoTemplate, fio)).append('\n');
        }

        sb.append('\t').append(messageBundle.getMessage("notification.overdue.header")).append('\n');

        DateTimeFormatter df = DateTimeFormatter.ofPattern(
                messageBundle.getMessage("notification.dateFormat"),
                Locale.getDefault()
        );

        for (OverdueItemDto item : payload.items()) {
            sb.append("\t\t").append(itemLabel(item.type())).append(' ')
                    .append('(').append(item.date() == null ? "" : df.format(item.date())).append(')')
                    .append('\n');
        }

        return sb.toString().trim();
    }

    private String itemLabel(OverdueItemType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case VEHICLE_INSURANCE -> messageBundle.getMessage("notification.overdue.item.vehicleInsurance");
            case ID_CARD_UNTIL -> messageBundle.getMessage("notification.overdue.item.idCardUntil");
        };
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
