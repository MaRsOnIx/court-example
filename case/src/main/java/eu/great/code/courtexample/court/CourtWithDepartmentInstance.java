package eu.great.code.courtexample.court;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
class CourtWithDepartmentInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID courtUuid;
    private String courtName;
    private String departmentName;

    protected CourtWithDepartmentInstance() {}

    public CourtWithDepartmentInstance(String courtName, String departmentName) {
        this.courtName = courtName;
        this.departmentName = departmentName;
    }

    UUID getCourtUuid() {
        return courtUuid;
    }

    String getCourtName() {
        return courtName;
    }

    String getDepartmentName() {
        return departmentName;
    }

    CourtWithDepartmentView toView(){
        return new CourtWithDepartmentView(courtUuid, courtName, departmentName);
    }
}
