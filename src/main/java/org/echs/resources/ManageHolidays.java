package org.echs.resources;

import org.echs.model.Holiday;
import org.echs.service.HolidayService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/holidays")
@Produces(value ={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ManageHolidays {

    HolidayService service = new HolidayService();

    @GET
    public Response getHolidays() throws Exception {
        return Response.status(Response.Status.OK).entity(service.getHolidaysList()).build();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createHoliday(List<Holiday> holidays) throws Exception {
        service.createHolidayEntry(holidays);
        return Response.ok().build();
    }
}
