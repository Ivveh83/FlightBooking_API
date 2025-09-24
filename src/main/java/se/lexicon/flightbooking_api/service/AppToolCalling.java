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

    private FlightBookingRepository flightBookingRepository;
    private FlightBookingMapper mapper;

    @Autowired
    public AppToolCalling(FlightBookingRepository flightBookingRepository,  FlightBookingMapper mapper) {
        this.flightBookingRepository = flightBookingRepository;
        this.mapper = mapper;
    }



    public AppToolCalling() {
    }

    @Tool(description = "Add a booking to the database")
    public String createBooking(Long flightId, String email, String name) {
        if (flightId == null) {
            return "flightId can not be null";
        } else if (name == null || name.isEmpty()) {
            return "name can not be null or empty";
        } else if (email == null || email.isEmpty()) {
            return "email can not be null or empty";
        }

        System.out.println("Adding New Booking: " + "flightId: " + flightId + " email: " + email + " name: " + name);
        Optional<FlightBooking> flightOptional = flightBookingRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
            return "Flight with id " + flightId + " does not exist";
        }
        FlightBooking flight = flightOptional.get();

        if (flight.getStatus() != FlightStatus.AVAILABLE) {
            return  "Flight with id " + flightId + " is not available";
        }

        flight.setPassengerName(name);
        flight.setPassengerEmail(email);
        flight.setStatus(FlightStatus.BOOKED);

        FlightBooking savedFlight = flightBookingRepository.save(flight);
        FlightBookingDTO dto = mapper.toDTO(savedFlight);
        return dto.toString();
    }


    @Tool(description = "Get all bookings for a specific email")
    public String checkBooking(String email) {
        System.out.println("Finding by email: " + email);
        List<FlightBooking> result = flightBookingRepository.findByPassengerEmail(email);
        if (result.isEmpty()) {
            System.out.println("No bookings under this email: " + email);
            return "There are no bookings under this email: " + email;
        }else  {
            List<FlightBookingDTO> dtoList = result.stream().map(fb -> mapper.toDTO(fb)).toList();
            List<String> list = dtoList.stream().map(Record::toString).toList();
            return "Bookings found: " + list;
        }
    }

    @Tool(description = "Cancel a booking from the database")
    public String cancelBooking(Long flightId, String email) {
        if (flightId == null) {
            return "flightId can not be null";
        } else if (email == null || email.isEmpty()) {
            return "email can not be null or empty";
        }
        System.out.println("Canceling booking with flightId: " + flightId);
        Optional<FlightBooking> flightOptional = flightBookingRepository.findById(flightId);
        if (flightOptional.isEmpty()) {
            return "Flight with id " + flightId + " does not exist";
        }
        FlightBooking flight = flightOptional.get();

        if (flight.getStatus() == FlightStatus.AVAILABLE) {
            return  "Flight with id " + flightId + " is already available";
        }
        flight.setStatus(FlightStatus.AVAILABLE);
        flight.setPassengerEmail(null);
        flight.setPassengerName(null);
        FlightBooking savedFlight  = flightBookingRepository.save(flight);
        FlightBookingDTO dto = mapper.toDTO(savedFlight);
        return "Booking was successfully canceled: " + dto.toString();
    }
}
