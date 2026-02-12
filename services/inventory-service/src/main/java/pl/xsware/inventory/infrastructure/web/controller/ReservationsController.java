package pl.xsware.inventory.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.xsware.inventory.application.query.InventoryQueryService;
import pl.xsware.inventory.infrastructure.web.dto.StockReservationDto;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class ReservationsController {

    private final InventoryQueryService queryService;

    @GetMapping("/reservations/{reservationId}")
    public StockReservationDto getReservation(@PathVariable UUID reservationId) {
        log.info("GET /inventory/reservations/{}", reservationId);

        return queryService.findReservation(reservationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Reservation not found: " + reservationId));
    }

    @GetMapping("/reservations")
    public List<StockReservationDto> getReservationsByOrderId(
        @RequestParam UUID orderId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        log.info("GET /inventory/reservations orderId={} limit={}", orderId, limit);
        return queryService.findReservationsByOrderId(orderId, limit);
    }
}
