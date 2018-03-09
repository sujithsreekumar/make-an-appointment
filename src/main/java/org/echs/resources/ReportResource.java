package org.echs.resources;

import org.echs.model.Booking;
import org.echs.model.BookingEntity;
import org.echs.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/appointments")
public class ReportResource {

    private static final Logger logger = LoggerFactory.getLogger(ReportResource.class);
    BookingService bookingService = new BookingService();


    @GET
    @Produces("application/pdf")
    public Response getFile() throws Exception {
        logger.info("Generating Today's Booking Report..");
        bookingService.generateReport();
        logger.info("Generating Response object");
        try {
            Response.ResponseBuilder response = Response.ok((Object) new FileInputStream("sms_bookings.pdf"));
            response.header("Content-Disposition",
                    "attachment; filename=\"sms_bookings.pdf\"");
            return response.build();
        }
        catch (Exception e) {
            logger.error("Error building Response : ", e);
            return  Response.serverError().build();
        }

//        try {
//            bookingService.generateReport();
//            return Response.ok().build();
//        }
//        catch (Exception e) {
//            return Response.serverError().build();
//        }
    }

    private List<Booking> mapToBooking(List<BookingEntity> bookingEntities) {
        return bookingEntities.stream()
                .map(Booking::new)
                .collect(toList());
    }

}
