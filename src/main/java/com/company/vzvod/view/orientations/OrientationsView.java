package com.company.vzvod.view.orientations;

import com.company.vzvod.orientations.OrientationsBrowseService;
import com.company.vzvod.orientations.OrientationsListUpdater;
import com.company.vzvod.orientations.component.OrientationsClient;
import com.company.vzvod.orientations.dto.DocumentFileDto;
import com.company.vzvod.orientations.dto.OrientationDto;
import com.company.vzvod.orientations.dto.OrientationImageDto;
import com.company.vzvod.orientations.dto.ScanResponse;
import com.company.vzvod.security.UiAccessService;
import com.company.vzvod.view.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Route(value = "orientations", layout = MainView.class)
@ViewController(id = "OrientationsView")
@ViewDescriptor(path = "orientations-view.xml")
public class OrientationsView extends StandardView {

    private static final String MSG_PREFIX = "com.company.vzvod.view.orientations/orientationsView.";

    @Autowired
    private OrientationsBrowseService orientationsBrowseService;

    @Autowired
    private UiAccessService uiAccessService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Messages messages;

    @Autowired
    private Notifications notifications;

    private final Span defaultPathLabel = new Span();
    private final Span counterLabel = new Span();
    private final Paragraph textParagraph = new Paragraph();
    private final Div photosContainer = new Div();
    private final Div emptyState = new Div();
    private final JmixButton previousButton = new JmixButton();
    private final JmixButton nextButton = new JmixButton();
    private final JmixButton copyButton = new JmixButton();
    private final JmixButton pickFolderButton = new JmixButton();
    private final OrientationsClient orientationsClient = new OrientationsClient();

    private final List<OrientationDto> orientations = new ArrayList<>();
    private int currentIndex = -1;
    private String scanSessionId;

    @Subscribe
    public void onInit(InitEvent event) {
        pickFolderButton.setText(messages.getMessage(MSG_PREFIX + "pickFolder"));
        previousButton.setText(messages.getMessage(MSG_PREFIX + "previous"));
        nextButton.setText(messages.getMessage(MSG_PREFIX + "next"));
        copyButton.setText(messages.getMessage(MSG_PREFIX + "copy"));
        emptyState.setText(messages.getMessage(MSG_PREFIX + "emptyState"));

        photosContainer.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "12px")
                .set("justify-content", "center");

        textParagraph.getStyle().set("white-space", "pre-wrap");

        previousButton.addClickListener(e -> onPrevious());
        nextButton.addClickListener(e -> onNext());
        copyButton.addClickListener(e -> onCopy());
        pickFolderButton.addClickListener(e -> orientationsClient.openFolderPicker());

        orientationsClient.setFilesPayloadListener(this::handleFilesPayload);
        orientationsClient.setCopiedListener(() -> notifications.create(messages.getMessage(MSG_PREFIX + "copied"))
                .withType(Notifications.Type.SUCCESS)
                .show());
        orientationsClient.setClientErrorListener(message -> notifications.create(message)
                .withType(Notifications.Type.ERROR)
                .show());

        getContent().removeAll();
        getContent().add(
                defaultPathLabel,
                pickFolderButton,
                orientationsClient,
                counterLabel,
                photosContainer,
                textParagraph,
                emptyState,
                new HorizontalLayout(previousButton, nextButton, copyButton)
        );

        updateNavigationState();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (!uiAccessService.hasFullAccessRole()) {
            throw new AccessDeniedException("Orientations require FullAccessRole");
        }
        try {
            defaultPathLabel.setText(messages.getMessage(MSG_PREFIX + "defaultPathPrefix")
                    + " " + orientationsBrowseService.fetchDefaultPath());
        } catch (RestClientException e) {
            defaultPathLabel.setText(messages.getMessage(MSG_PREFIX + "defaultPathUnavailable"));
        }
        orientationsClient.tryRestoreSavedFolder();
    }

    private void handleFilesPayload(String jsonPayload) {
        try {
            JsonNode root = objectMapper.readTree(jsonPayload);
            JsonNode filesNode = root.get("files");
            List<DocumentFileDto> files = new ArrayList<>();
            if (filesNode != null && filesNode.isArray()) {
                Iterator<JsonNode> iterator = filesNode.elements();
                while (iterator.hasNext()) {
                    JsonNode fileNode = iterator.next();
                    files.add(new DocumentFileDto(
                            fileNode.path("fileName").asText(),
                            fileNode.path("contentBase64").asText()
                    ));
                }
            }
            ScanResponse scanResponse = orientationsBrowseService.scan(scanSessionId, files);
            scanSessionId = scanResponse.sessionId();
            List<OrientationDto> scanned = scanResponse.orientations();
            OrientationsListUpdater.UpdateResult update = OrientationsListUpdater.merge(
                    orientations,
                    currentIndex,
                    scanned
            );
            if (!update.changed()) {
                return;
            }
            orientations.clear();
            orientations.addAll(update.orientations());
            currentIndex = update.currentIndex();
            renderCurrentOrientation();
        } catch (Exception e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "scanFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private void onPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            renderCurrentOrientation();
        }
    }

    private void onNext() {
        if (currentIndex >= 0 && currentIndex < orientations.size() - 1) {
            currentIndex++;
            renderCurrentOrientation();
        }
    }

    private void onCopy() {
        if (currentIndex < 0 || currentIndex >= orientations.size()) {
            return;
        }
        OrientationDto current = orientations.get(currentIndex);
        try {
            orientationsClient.copyToClipboard(current.text(), objectMapper.writeValueAsString(current.images()));
        } catch (JsonProcessingException e) {
            notifications.create(messages.getMessage(MSG_PREFIX + "scanFailed"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    private void renderCurrentOrientation() {
        photosContainer.removeAll();
        if (currentIndex < 0 || currentIndex >= orientations.size()) {
            textParagraph.setText("");
            counterLabel.setText("");
            photosContainer.setVisible(false);
            emptyState.setVisible(true);
            updateNavigationState();
            return;
        }

        OrientationDto current = orientations.get(currentIndex);
        emptyState.setVisible(false);
        photosContainer.setVisible(true);
        textParagraph.setText(current.text());
        for (OrientationImageDto image : current.images()) {
            Image photoImage = new Image();
            photoImage.setMaxWidth("100%");
            photoImage.getStyle().set("max-height", "420px");
            photoImage.setSrc("data:" + image.contentType() + ";base64," + image.base64());
            photosContainer.add(photoImage);
        }
        counterLabel.setText(String.format(
                messages.getMessage(MSG_PREFIX + "counter"),
                currentIndex + 1,
                orientations.size()
        ));
        updateNavigationState();
    }

    private void updateNavigationState() {
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex >= 0 && currentIndex < orientations.size() - 1);
        copyButton.setEnabled(currentIndex >= 0 && currentIndex < orientations.size());
    }
}
