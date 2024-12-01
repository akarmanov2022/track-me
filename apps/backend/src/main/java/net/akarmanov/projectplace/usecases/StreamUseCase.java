package net.akarmanov.projectplace.usecases;

import lombok.RequiredArgsConstructor;
import net.akarmanov.projectplace.domain.Stream;
import net.akarmanov.projectplace.mapping.StreamMapper;
import net.akarmanov.projectplace.rest.api.dto.StreamCreateDto;
import net.akarmanov.projectplace.rest.api.dto.StreamDto;
import net.akarmanov.projectplace.rest.api.dto.StreamUpdateDto;
import net.akarmanov.projectplace.services.stream.StreamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StreamUseCase {

    private final StreamMapper streamMapper;

    private final StreamService streamService;

    public StreamDto createStream(StreamCreateDto streamCreateDto) {
        var stream = buildStream(streamCreateDto);
        stream = streamService.create(stream);
        return streamMapper.mapToDto(stream);
    }

    public StreamDto updateStream(UUID streamId, StreamUpdateDto stream) {
        return null;
    }

    public void deleteStream(UUID streamId) {

    }

    public Page<StreamDto> findAllStreams(Pageable pageable) {
        return streamService.find(pageable)
                .map(streamMapper::mapToDto);
    }

    public StreamDto getById(UUID streamId) {
        return null;
    }

    private Stream buildStream(StreamCreateDto stream) {
        return Stream.builder()
                .endDate(stream.endDate())
                .startDate(stream.startDate() == null ? LocalDate.now() : stream.startDate())
                .name(stream.name())
                .build();
    }
}
