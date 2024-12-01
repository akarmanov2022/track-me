package net.akarmanov.projectplace.mapping;

import net.akarmanov.projectplace.domain.Stream;
import net.akarmanov.projectplace.rest.api.dto.StreamDto;
import net.akarmanov.projectplace.rest.api.dto.StreamUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ON_IMPLICIT_CONVERSION)
public interface StreamMapper {

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "teamCards", ignore = true)
    Stream mapFromDto(StreamDto dto);

    StreamDto mapToDto(Stream entity);

    List<Stream> mapFromDto(List<StreamDto> dtos);

    List<StreamDto> mapToDto(List<Stream> entities);

    @Mapping(target = "users", ignore = true)
    @Mapping(target = "teamCards", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromDto(StreamUpdateDto dto, @MappingTarget Stream entity);
}
