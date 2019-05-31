function MarkLeave(data) {
    var self = this;
    self.department = ko.observable(data.department);
    self.doctorName = ko.observable(data.doctorName);
    self.dates = ko.observableArray(data.dates);
}

function LeaveViewModel() {
    var self = this;

    $.getJSON("/department", function(allData) {
        var mappedDepartments = $.map(allData, function(item) { return new department(item) });
        self.department(mappedDepartments);
    });

    $.getJSON("/department/doctors", function(allData) {
        var mappedDepartments = $.map(allData, function(item) { return new department(item) });
        self.department(mappedDepartments);
    });

    self.addSeat = function() {
        self.seats.push(new MarkLeave(self.department(), self.doctorName(), self.date()));
    }
}

ko.applyBindings(new LeaveViewModel());