package com.hotelier.service;

import com.hotelier.dto.GuestRequest;
import com.hotelier.exception.ResourceNotFoundException;
import com.hotelier.exception.ValidationException;
import com.hotelier.model.Guest;
import com.hotelier.repository.GuestRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestService {

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 \\-]{6,17}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    /** Lists all guests, or only those matching the search text (name, phone, e-mail or guest ID). */
    @Transactional(readOnly = true)
    public List<Guest> search(String query) {
        List<Guest> all = guestRepository.findAll();
        if (query == null || query.isBlank()) {
            return all;
        }
        String q = query.trim().toLowerCase();
        return all.stream()
                .filter(g -> contains(g.getName(), q) || contains(g.getPhone(), q)
                        || contains(g.getEmail(), q) || contains(g.getGuestCode(), q))
                .toList();
    }

    @Transactional(readOnly = true)
    public Guest get(Long id) throws ResourceNotFoundException {
        return guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest " + id + " was not found"));
    }

    @Transactional
    public Guest create(GuestRequest request) throws ValidationException {
        validate(request);
        Guest guest = new Guest(request.name(), request.phone().trim(), blankToNull(request.email()));
        guest = guestRepository.save(guest);
        guest.setGuestCode("G-" + (1000 + guest.getId()));   // unique, human-friendly guest ID
        return guest;
    }

    @Transactional
    public Guest update(Long id, GuestRequest request) throws ValidationException, ResourceNotFoundException {
        validate(request);
        Guest guest = get(id);
        guest.setName(request.name());
        guest.setPhone(request.phone().trim());
        guest.setEmail(blankToNull(request.email()));
        return guest;
    }

    @Transactional(readOnly = true)
    public long count() {
        return guestRepository.count();
    }

    private void validate(GuestRequest r) throws ValidationException {
        if (r == null || r.name() == null || r.name().trim().length() < 2) {
            throw new ValidationException("Guest name must have at least 2 characters");
        }
        if (r.name().trim().length() > 80) {
            throw new ValidationException("Guest name is too long (maximum 80 characters)");
        }
        if (r.phone() == null || !PHONE.matcher(r.phone().trim()).matches()) {
            throw new ValidationException("Enter a valid phone number (7 to 18 digits, optional + at the start)");
        }
        if (r.email() != null && !r.email().isBlank() && !EMAIL.matcher(r.email().trim()).matches()) {
            throw new ValidationException("Enter a valid e-mail address or leave it blank");
        }
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
