package net.akarmanov.projectplace.services.stream;

import net.akarmanov.projectplace.domain.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StreamService {
    Stream create(Stream createdStream);

    Stream getById(UUID id);

    Stream getCurrentStream();

    void save(Stream stream);

    Page<Stream> find(Pageable pageable);
}
