package eu.great.code.courtexample.court;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
class CaseInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID caseUuid;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private boolean active = true;
    private int numer;
    @Enumerated(EnumType.STRING)
    private Symbol symbol;
    @OneToOne
    private CourtWithDepartmentInstance courtWithDepartmentInstance;
    private UUID lastAssignedJudgeUuid;
    private String przedmiotSprawy;
    private LocalDate dataPierwotnegoWplywu;
    private LocalDate dataWplywu;

    CaseInstance(UUID judgeUuid, CourtWithDepartmentInstance courtWithDepartment, int numer, Symbol symbol, String przedmiotSprawy, LocalDate dataPierwotnegoWplywu, LocalDate dataWplywu) {
        this.lastAssignedJudgeUuid = judgeUuid;
        this.courtWithDepartmentInstance = courtWithDepartment;
        this.numer = numer;
        this.symbol = symbol;
        this.przedmiotSprawy = przedmiotSprawy;
        this.dataPierwotnegoWplywu = dataPierwotnegoWplywu;
        this.dataWplywu = dataWplywu;
    }

    protected CaseInstance() {}

    private String getSygnatura(){
        return symbol.getText() + " " + numer + "/" + String.format("%02d", dataPierwotnegoWplywu.getYear() % 100);
    }

    void changeJudge(UUID judgeUuid){
        if(!active){
            throw new IllegalStateException("Nie można przypisać sędziego do sprawy, która jest nieaktywna");
        }
        if(judgeUuid == null){
            throw new IllegalStateException("Musi zostać wskazany sędzia");
        }
        this.lastAssignedJudgeUuid = judgeUuid;

    }

    void moveToOtherCourt(CourtWithDepartmentInstance courtWithDepartmentInstance){
        this.active = false;
        this.courtWithDepartmentInstance = courtWithDepartmentInstance;
    }

    public CaseSnapshot getSnapshot() {
        return new CaseSnapshot(
                caseUuid,
                active,
                getSygnatura(),
                lastAssignedJudgeUuid,
                przedmiotSprawy,
                dataPierwotnegoWplywu,
                dataWplywu,
                courtWithDepartmentInstance.getCourtName(),
                courtWithDepartmentInstance.getDepartmentName());
    }


}
