package ae.dar360.viewing.service;

import ae.dar360.viewing.model.Viewing;
import ae.dar360.viewing.repository.ViewingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ViewingService {
    private final ViewingRepository viewingRepository;

    public List<Viewing> findAll() {
        return viewingRepository.findAll();
    }

    public Optional<Viewing> findById(Long id) {
        return viewingRepository.findById(id);
    }

    public Viewing save(Viewing viewing) {
        return viewingRepository.save(viewing);
    }

    public void deleteById(Long id) {
        viewingRepository.deleteById(id);
    }
}
