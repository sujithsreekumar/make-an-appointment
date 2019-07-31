var LeaveLine = function () {
    var self = this;
    self.department = ko.observable();
    self.doctor = ko.observable();
    self.fromdate = ko.observable();
    self.todate = ko.observable();
};

var doctors = function () {
    var tmp = null;
    $.ajax({
        'async': false,
        'type': "GET",
        'global': false,
        'url': "webapi/resources/doctors",
        'success': function (data) {
            tmp = data;
        }
    });
    return tmp;
}();

var LeaveModel = function () {
    var self = this;
    // var json = docs;
    // alert("Json is : " + JSON.stringify(json));
    // self.docs = ko.observableArray(json);
    // alert("docs is : " + self.docs())
    // self.accid = ko.observable("Sujith");

    self.lines = ko.observableArray([new LeaveLine()]);

    //operations
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
                department: line.department().department,
                fromDate: line.fromdate(),
                toDate: line.todate()
            } : undefined
        });
        alert("Could now send this to server: " + JSON.stringify(dataToSave));
        $.ajax({
            url: 'webapi/leaves/mark/',
            type: 'PUT',
            data: ko.toJSON(dataToSave),
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            success: function (returnedData) {
                alert("Response: " + JSON.stringify(returnedData));
            }
        });
    };
};

$(function () {
    ko.applyBindings(new LeaveModel());
});
