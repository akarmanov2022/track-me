package net.akarmanov.projectplace.rest.api.admin;

import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.rest.api.dto.StreamCreateDto;
import net.akarmanov.projectplace.rest.api.dto.StreamDto;
import net.akarmanov.projectplace.rest.api.dto.StreamUpdateDto;
import net.akarmanov.projectplace.usecases.StreamUseCase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class StreamAdminRestControllerImpl implements StreamAdminRestController {

    private final StreamUseCase streamUseCase;

    @Override
    public ResponseEntity<StreamDto> create(StreamCreateDto stream) {
        var streamDto = streamUseCase.createStream(stream);
        return ResponseEntity.ok(streamDto);
    }

    @Override
    public ResponseEntity<StreamDto> update(UUID streamId, StreamUpdateDto stream) {
        var streamDto = streamUseCase.updateStream(streamId, stream);
        return ResponseEntity.ok(streamDto);
    }

    @Override
    public ResponseEntity<Void> delete(UUID streamId) {
        streamUseCase.deleteStream(streamId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public PagedModel<StreamDto> findAll(Pageable pageable) {
        var streams = streamUseCase.findAllStreams(pageable);
        return new PagedModel<>(streams);
    }

    @Override
    public ResponseEntity<StreamDto> getById(UUID streamId) {
        var stream = streamUseCase.getById(streamId);
        return ResponseEntity.ok(stream);
    }
}
