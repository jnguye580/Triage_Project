import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class WildfireHelicopterTriageTest {

    @Test
    public void testCriticalPatientGetsPriority1AndCorrectResourceCost() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(20, 5, 3, 1);

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("Maria", true, false, false, false, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_1, patient.getPriorityLevel());
        assertEquals(5, patient.getResourceNeeded());
        assertEquals(1, patient.getArrivalOrder());
    }

    @Test
    public void testSeriousPatientGetsPriority2AndCorrectResourceCost() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(20, 5, 3, 1);

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("James", false, true, false, false, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_2, patient.getPriorityLevel());
        assertEquals(3, patient.getResourceNeeded());
    }

    @Test
    public void testStablePatientGetsPriority3AndCorrectResourceCost() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(20, 5, 3, 1);

        WildfireHelicopterTriage.Patient patient =
                system.addPatient("David", false, false, true, true, true);

        assertEquals(WildfireHelicopterTriage.PriorityLevel.PRIORITY_3, patient.getPriorityLevel());
        assertEquals(1, patient.getResourceNeeded());
    }

    @Test
    public void testAddPatientRejectsInvalidConditionCombination() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        assertThrows(IllegalArgumentException.class, () ->
                system.addPatient("Invalid1", true, true, false, false, true));

        assertThrows(IllegalArgumentException.class, () ->
                system.addPatient("Invalid2", false, false, false, false, true));
    }

    @Test
    public void testAddPatientRejectsEmptyName() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        assertThrows(IllegalArgumentException.class, () ->
                system.addPatient("   ", true, false, false, false, true));
    }

    @Test
    public void testConstructorRejectsInvalidResourceValues() {
        assertThrows(IllegalArgumentException.class, () ->
                new WildfireHelicopterTriage.TriageSystem(-1, 4, 2, 1));

        assertThrows(IllegalArgumentException.class, () ->
                new WildfireHelicopterTriage.TriageSystem(10, 0, 2, 1));

        assertThrows(IllegalArgumentException.class, () ->
                new WildfireHelicopterTriage.TriageSystem(10, 4, -2, 1));

        assertThrows(IllegalArgumentException.class, () ->
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 0));
    }

    @Test
    public void testGetEvacuationQueueFiltersAndSortsByPriorityThenArrival() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(20, 4, 2, 1);

        system.addPatient("David", false, false, true, true, true);
        system.addPatient("Liam", true, false, false, false, false);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("Elena", true, false, false, false, true);

        List<WildfireHelicopterTriage.Patient> queue = system.getEvacuationQueue();

        assertEquals(4, queue.size());
        assertEquals("Maria", queue.get(0).getName());
        assertEquals("Elena", queue.get(1).getName());
        assertEquals("James", queue.get(2).getName());
        assertEquals("David", queue.get(3).getName());
    }

    @Test
    public void testLoadHelicopterBoardsPatientsWithinCapacityAndResources() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(7, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("David", false, false, true, true, true);

        WildfireHelicopterTriage.LoadResult result = system.loadHelicopter(3);

        List<WildfireHelicopterTriage.Patient> boarded = result.getBoardedPatients();

        assertEquals(3, boarded.size());
        assertEquals("Maria", boarded.get(0).getName());
        assertEquals("James", boarded.get(1).getName());
        assertEquals("David", boarded.get(2).getName());

        assertTrue(result.getWaitingForResources().isEmpty());
        assertEquals(7, result.getResourcesBefore());
        assertEquals(0, result.getResourcesAfter());
        assertEquals(0, system.getAvailableResources());
        assertEquals(0, system.getAllPatients().size());
        assertEquals(3, system.getEscapedPatientsCount());
    }

    @Test
    public void testEscapedPatientsCountAccumulatesAcrossLoads() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("David", false, false, true, true, true);

        system.loadHelicopter(2);
        assertEquals(2, system.getEscapedPatientsCount());

        system.loadHelicopter(2);
        assertEquals(3, system.getEscapedPatientsCount());
    }

    @Test
    public void testStrictPriorityStopsWhenNextPatientCannotBeSupported() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(5, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("David", false, false, true, true, true);

        WildfireHelicopterTriage.LoadResult result = system.loadHelicopter(3);

        List<WildfireHelicopterTriage.Patient> boarded = result.getBoardedPatients();
        List<WildfireHelicopterTriage.Patient> waiting = result.getWaitingForResources();
        List<WildfireHelicopterTriage.Patient> remaining = system.getAllPatients();

        assertEquals(1, boarded.size());
        assertEquals("Maria", boarded.get(0).getName());

        assertEquals(1, waiting.size());
        assertEquals("James", waiting.get(0).getName());

        assertEquals(5, result.getResourcesBefore());
        assertEquals(1, result.getResourcesAfter());
        assertEquals(1, system.getAvailableResources());
        assertEquals(1, system.getEscapedPatientsCount());

        assertEquals(2, remaining.size());
        assertEquals("James", remaining.get(0).getName());
        assertEquals("David", remaining.get(1).getName());
    }

    @Test
    public void testLoadHelicopterRespectsSeatCapacity() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(20, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("Elena", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);

        WildfireHelicopterTriage.LoadResult result = system.loadHelicopter(2);

        List<WildfireHelicopterTriage.Patient> boarded = result.getBoardedPatients();
        List<WildfireHelicopterTriage.Patient> remaining = system.getAllPatients();

        assertEquals(2, boarded.size());
        assertEquals("Maria", boarded.get(0).getName());
        assertEquals("Elena", boarded.get(1).getName());

        assertEquals(20, result.getResourcesBefore());
        assertEquals(12, result.getResourcesAfter());
        assertEquals(2, system.getEscapedPatientsCount());

        assertEquals(1, remaining.size());
        assertEquals("James", remaining.get(0).getName());
    }

    @Test
    public void testNoHelicopterPatientsMeansNobodyBoards() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        system.addPatient("Ava", true, false, false, false, false);
        system.addPatient("Noah", false, true, false, false, false);

        WildfireHelicopterTriage.LoadResult result = system.loadHelicopter(2);

        assertTrue(result.getBoardedPatients().isEmpty());
        assertTrue(result.getWaitingForResources().isEmpty());
        assertEquals(10, result.getResourcesBefore());
        assertEquals(10, result.getResourcesAfter());
        assertEquals(2, system.getAllPatients().size());
        assertEquals(0, system.getEscapedPatientsCount());
    }

    @Test
    public void testLoadHelicopterRejectsInvalidCapacity() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> system.loadHelicopter(0));
        assertThrows(IllegalArgumentException.class, () -> system.loadHelicopter(-1));
    }

    @Test
    public void testLoadHelicopterAndReturnBoardedConvenienceMethod() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(6, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);
        system.addPatient("David", false, false, true, true, true);

        List<WildfireHelicopterTriage.Patient> boarded =
                system.loadHelicopterAndReturnBoarded(2);

        assertEquals(2, boarded.size());
        assertEquals("Maria", boarded.get(0).getName());
        assertEquals("James", boarded.get(1).getName());

        assertEquals(0, system.getAvailableResources());
        assertEquals(2, system.getEscapedPatientsCount());
        assertEquals(1, system.getAllPatients().size());
        assertEquals("David", system.getAllPatients().get(0).getName());
    }

    @Test
    public void testGenerateRandomPatientsCreatesRequestedNumber() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(50, 4, 2, 1);

        List<WildfireHelicopterTriage.Patient> generated = system.generateRandomPatients(10);

        assertEquals(10, generated.size());
        assertEquals(10, system.getAllPatients().size());
        assertEquals(10, system.getEvacuationQueue().size());
    }

    @Test
    public void testGenerateRandomPatientsCreatesValidPatients() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(50, 4, 2, 1);

        List<WildfireHelicopterTriage.Patient> generated = system.generateRandomPatients(15);

        for (int i = 0; i < generated.size(); i++) {
            WildfireHelicopterTriage.Patient patient = generated.get(i);

            assertNotNull(patient.getName());
            assertFalse(patient.getName().trim().isEmpty());
            assertEquals(i + 1, patient.getArrivalOrder());
            assertTrue(patient.needsHelicopter());

            int conditionCount = 0;
            if (patient.isCriticalCondition()) {
                conditionCount++;
            }
            if (patient.isSeriousCondition()) {
                conditionCount++;
            }
            if (patient.isStableCondition()) {
                conditionCount++;
            }
            assertEquals(1, conditionCount);

            switch (patient.getPriorityLevel()) {
                case PRIORITY_1:
                    assertTrue(patient.isCriticalCondition());
                    assertEquals(4, patient.getResourceNeeded());
                    break;
                case PRIORITY_2:
                    assertTrue(patient.isSeriousCondition());
                    assertEquals(2, patient.getResourceNeeded());
                    break;
                case PRIORITY_3:
                    assertTrue(patient.isStableCondition());
                    assertEquals(1, patient.getResourceNeeded());
                    break;
            }
        }
    }

    @Test
    public void testGenerateRandomPatientsRejectsInvalidCount() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(50, 4, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> system.generateRandomPatients(0));
        assertThrows(IllegalArgumentException.class, () -> system.generateRandomPatients(-5));
    }

    @Test
    public void testGetRemainingPatientsCount() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);
        system.addPatient("James", false, true, false, false, true);

        assertEquals(2, system.getRemainingPatientsCount());

        system.loadHelicopter(1);

        assertEquals(1, system.getRemainingPatientsCount());
    }

    @Test
    public void testHasPatientsWaiting() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(10, 4, 2, 1);

        assertFalse(system.hasPatientsWaiting());

        system.addPatient("Maria", true, false, false, false, true);

        assertTrue(system.hasPatientsWaiting());
    }

    @Test
    public void testIsOutOfResources() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(4, 4, 2, 1);

        assertFalse(system.isOutOfResources());

        system.addPatient("Maria", true, false, false, false, true);
        system.loadHelicopter(1);

        assertTrue(system.isOutOfResources());
        assertEquals(0, system.getAvailableResources());
    }

    @Test
    public void testCannotEvacuateNextPatientWhenResourcesAreTooLow() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(3, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);

        assertTrue(system.cannotEvacuateNextPatient());
    }

    @Test
    public void testCannotEvacuateNextPatientFalseWhenEnoughResourcesExist() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(4, 4, 2, 1);

        system.addPatient("Maria", true, false, false, false, true);

        assertFalse(system.cannotEvacuateNextPatient());
    }

    @Test
    public void testCannotEvacuateNextPatientFalseWhenNoPatientsWaiting() {
        WildfireHelicopterTriage.TriageSystem system =
                new WildfireHelicopterTriage.TriageSystem(1, 4, 2, 1);

        assertFalse(system.cannotEvacuateNextPatient());
    }
}
