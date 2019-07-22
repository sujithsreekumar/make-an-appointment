var LeaveLine = function () {
    var self = this;
    self.department = ko.observable();
    self.doctor = ko.observable();
    self.department.subscribe(function() {
        self.doctor(undefined);
    });
};

var LeaveModel = function () {
    var self = this;
    self.lines = ko.observableArray([new LeaveLine()]);

    //operations
    self.addLine = function() { self.lines.push(new LeaveLine())};
    self.removeLine = function(line) { self.lines.remove(line) };
    self.save = function() {
        var dataToSave = $.map(self.lines(), function(line) {
            return line.doctor() ? {
                doctorName: line.doctor().name,
                departmentName: line.department.department
            } : undefined
        });
        alert("Could now send this to server: " + JSON.stringify(dataToSave));
    };

    var doctors = $getJSON()
};

$(function() {
    ko.applyBindings(new LeaveModel());
});
