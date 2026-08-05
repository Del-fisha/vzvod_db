package com.company.vzvod.orientations;

import com.company.vzvod.orientations.client.OrientationsServiceClient;
import com.company.vzvod.orientations.dto.DocumentFileDto;
import com.company.vzvod.orientations.dto.OrientationDto;
import com.company.vzvod.orientations.dto.OrientationImageDto;
import com.company.vzvod.orientations.dto.ScanResponse;
import com.company.vzvod.security.UiAccessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Просмотр ориентировок")
class OrientationsBrowseServiceTest {

    @Mock
    OrientationsServiceClient client;

    @Mock
    UiAccessService uiAccessService;

    @InjectMocks
    OrientationsBrowseService service;

    @Test
    @DisplayName("scan требует FullAccessRole")
    void scanRequiresFullAccess() {
        when(uiAccessService.hasFullAccessRole()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.scan(null, List.of()));
    }

    @Test
    @DisplayName("scan возвращает ориентировки от микросервиса")
    void scanReturnsOrientations() {
        when(uiAccessService.hasFullAccessRole()).thenReturn(true);
        OrientationDto dto = new OrientationDto("a.docx", "Текст", List.of(new OrientationImageDto("aW1n", "image/png")));
        when(client.scan(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ScanResponse("session-1", List.of(dto)));

        ScanResponse result = service.scan(null, List.of(new DocumentFileDto("a.docx", "Y29udGVudA==")));

        assertEquals(1, result.orientations().size());
        assertEquals("Текст", result.orientations().get(0).text());
        assertEquals("session-1", result.sessionId());
        verify(client).scan(org.mockito.ArgumentMatchers.any());
    }
}
