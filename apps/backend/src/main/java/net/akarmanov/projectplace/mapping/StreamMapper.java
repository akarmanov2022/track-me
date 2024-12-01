package net.akarmanov.projectplace.mapping;

import net.akarmanov.projectplace.domain.Stream;
import net.akarmanov.projectplace.rest.api.dto.StreamDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StreamMapper {

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "teamCards", ignore = true)
    Stream mapFromDto(StreamDto dto);

    StreamDto mapToDto(Stream entity);

    List<Stream> mapFromDto(List<StreamDto> dtos);

    List<StreamDto> mapToDto(List<Stream> entities);
}
