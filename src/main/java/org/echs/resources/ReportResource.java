package org.echs.resources;

import org.echs.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;

@Path("/appointments")
public class ReportResource {

    private static final Logger logger = LoggerFactory.getLogger(ReportResource.class);
    BookingService bookingService = new BookingService();


    @GET
    @Produces("application/pdf")
    public Response getFile() throws Exception {
        logger.info("Generating Today's Booking Report..");
        byte[] file = bookingService.generateReport();
        try {
            Response.ResponseBuilder response = Response.ok((Object) new ByteArrayInputStream(file));
            response.header("Content-Disposition",
                    "attachment; filename=\"sms_bookings.pdf\"");
            return response.build();
        }
        catch (Exception e) {
            logger.error("Error building Response : ", e);
            return  Response.serverError().build();
        }
    }
}
