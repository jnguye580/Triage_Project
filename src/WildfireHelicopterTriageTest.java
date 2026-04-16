import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class WildfireHelicopterTriageTest {

    @Test
    public void testCriticalPatientGetsPriority1() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("Maria", true, false, false, false, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_1, patient.getPriorityLevel());
    }

    @Test
    public void testSeriousPatientGetsPriority2() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("James", false, true, false, false, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_2, patient.getPriorityLevel());
    }

    @Test
    public void testStablePatientGetsPriority3() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("David", false, false, true, true, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_3, patient.getPriorityLevel());
    }

    @Test
    public void testEvacuationQueueSortedByPriorityThenArrival() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        system.addPatient("David", false, false, true, true, true);   // P3
        system.addPatient("James", false, true, false, false, true);  // P2
        system.addPatient("Maria", true, false, false, false, true);  // P1
        system.addPatient("Elena", true, false, false, false, true);  // P1

        List<WildfireHelicopterTriage.Patient> queue = system.getEvacuationQueue();

        assertEquals("Maria", queue.get(0).getName());
        assertEquals("Elena", queue.get(1).getName());
        assertEquals("James", queue.get(2).getName());
        assertEquals("David", queue.get(3).getName());
    }

    @Test
    public void testOnlyHelicopterPatientsAppearInEvacuationQueue() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("Liam", true, false, false, false, false);

        List<WildfireHelicopterTriage.Patient> queue = system.getEvacuationQueue();

        assertEquals(1, queue.size());
        assertEquals("Maria", queue.get(0).getName());
    }

    @Test
    public void testLoadHelicopterBoardsHighestPriorityPatients() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        system.addPatient("David", false, false, true, true, true);   // P3
        system.addPatient("James", false, true, false, false, true);  // P2
        system.addPatient("Maria", true, false, false, false, true);  // P1
        system.addPatient("Elena", true, false, false, false, true);  // P1

        List<WildfireHelicopterTriage.Patient> boarded =
                system.loadHelicopterAndReturnBoarded(2);

        assertEquals(2, boarded.size());
        assertEquals("Maria", boarded.get(0).getName());
        assertEquals("Elena", boarded.get(1).getName());
    }

    @Test
    public void testBoardedPatientsAreRemovedFromSystem() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        system.addPatient("David", false, false, true, true, true);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("Maria", true, false, false, false, true);

        system.loadHelicopterAndReturnBoarded(1);

        List<WildfireHelicopterTriage.Patient> remaining = system.getAllPatients();

        assertEquals(2, remaining.size());
        assertFalse(remaining.stream().anyMatch(p -> p.getName().equals("Maria")));
    }

    @Test
    public void testLoadHelicopterReturnsEmptyListWhenNobodyNeedsEvacuation() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem();

        system.addPatient("Ava", true, false, false, false, false);
        system.addPatient("Noah", false, true, false, false, false);

        List<WildfireHelicopterTriage.Patient> boarded =
                system.loadHelicopterAndReturnBoarded(2);

        assertTrue(boarded.isEmpty());
    }
}
