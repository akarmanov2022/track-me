package net.akarmanov.projectplace.rest.api.stream;

import io.swagger.v3.oas.annotations.Operation;
import net.akarmanov.projectplace.rest.api.dto.StreamDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/streams")
public interface StreamRestController {
    @Operation(summary = "Получить текущий поток", description = "Возвращает информацию о текущем потоке")
    @GetMapping("/current")
    ResponseEntity<StreamDto> getCurrentStream();

    @Operation(summary = "Получить список потоков", description = "Возвращает список потоков с поддержкой пагинации")
    @GetMapping
    PagedModel<StreamDto> getStreams(@PageableDefault @ParameterObject Pageable pageable);
}
