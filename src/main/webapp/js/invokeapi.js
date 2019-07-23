var LeaveLine = function () {
    var self = this;
    // self.doctors = $.getJSON("http://localhost:8080/smsbooking/webapi/resources/doctors", function (data) {
    //     return ko.mapping.fromJS(data);
    // });
    // console.log(self.doctors);
    self.valueString1 = ko.observable();
    self.value2 = ko.observable();
    self.department = ko.observable();
    self.doctor = ko.observable();
    self.department.subscribe(function () {
        self.doctor(undefined);
    });
};

var LeaveModel = function () {
    var self = this;
    self.lines = ko.observableArray([new LeaveLine()]);
    self.addLine = function () {
        self.lines.push(new LeaveLine())
    };
    self.removeLine = function (line) {
        self.lines.remove(line)
    };
    self.save = function () {
        var dataToSave = $.map(self.lines(), function (line) {
            return line.doctor() ? {
                doctorName: line.doctor().name,
                departmentName: line.department.department
            } : undefined
        });
        alert("Could now send this to server: " + JSON.stringify(dataToSave));
    };

    ViewModel.prototype.to01012000 = function() {
        this.valueString1('01/01/2000');
        this.value2(new Date(2000, 0, 1));
    };
};


ko.applyBindings(new LeaveModel());

