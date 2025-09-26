package se.lexicon.flightbooking_api.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.FlightBookingDTO;
import se.lexicon.flightbooking_api.dto.QueryDto;
import se.lexicon.flightbooking_api.entity.FlightBooking;
import se.lexicon.flightbooking_api.entity.FlightStatus;
import se.lexicon.flightbooking_api.mapper.FlightBookingMapper;
import se.lexicon.flightbooking_api.repository.FlightBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AppToolCalling {

    private FlightBookingService flightBookingService;

    @Autowired
    public AppToolCalling(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }



    public AppToolCalling() {
    }

    @Tool(description = "Add a booking to the database")
    public String createBooking(Long flightId, BookFlightRequestDTO bookFlightRequestDTO) {
        try {
            FlightBookingDTO fbDto = flightBookingService.bookFlight(flightId, bookFlightRequestDTO);
            return "Successfully added booking" + fbDto;
        } catch (Exception ex) {
            return "Error by booking due to " +  ex.getMessage();
        }
    }


    @Tool(description = "Get all bookings for a specific email")
    public String checkBooking(String email) {
        try {
            List<FlightBookingDTO> result = flightBookingService.findBookingsByEmail(email);
            if (result.isEmpty()) {
                System.out.println("No bookings under this email: " + email);
                return "There are no bookings under this email: " + email;
            }
            return "Successfully found these bookings " + result + "  for " + email;
        } catch (Exception e) {
            return "Error by checking booking due to " +  e.getMessage();
        }
    }

    @Tool(description = "Cancel a booking from the database")
    public String cancelBooking(Long flightId, String email) {
        try {
            flightBookingService.cancelFlight(flightId, email);
            return "Successfully cancelled booking";
        } catch (Exception ex) {
            return "Error by canceling booking due to " + ex.getMessage();
        }
    }
}
