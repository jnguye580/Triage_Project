import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class WildfireHelicopterTriage {

    public enum PriorityLevel {
        PRIORITY_1(1, "Critical - evacuate first"),
        PRIORITY_2(2, "Serious - evacuate second"),
        PRIORITY_3(3, "Stable - evacuate last");

        private final int level;
        private final String description;

        PriorityLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class Patient {
        private final int arrivalOrder;
        private final String name;
        private final boolean criticalCondition;
        private final boolean seriousCondition;
        private final boolean stableCondition;
        private final boolean canWalk;
        private final boolean needsHelicopter;
        private final PriorityLevel priorityLevel;

        public Patient(int arrivalOrder, String name, boolean criticalCondition,
                       boolean seriousCondition, boolean stableCondition,
                       boolean canWalk, boolean needsHelicopter) {
            this.arrivalOrder = arrivalOrder;
            this.name = name;
            this.criticalCondition = criticalCondition;
            this.seriousCondition = seriousCondition;
            this.stableCondition = stableCondition;
            this.canWalk = canWalk;
            this.needsHelicopter = needsHelicopter;
            this.priorityLevel = assignPriority();
        }

        private PriorityLevel assignPriority() {
            if (criticalCondition) {
                return PriorityLevel.PRIORITY_1;
            } else if (seriousCondition) {
                return PriorityLevel.PRIORITY_2;
            } else {
                return PriorityLevel.PRIORITY_3;
            }
        }

        public int getArrivalOrder() {
            return arrivalOrder;
        }

        public String getName() {
            return name;
        }

        public boolean isCriticalCondition() {
            return criticalCondition;
        }

        public boolean isSeriousCondition() {
            return seriousCondition;
        }

        public boolean isStableCondition() {
            return stableCondition;
        }

        public boolean canWalk() {
            return canWalk;
        }

        public boolean needsHelicopter() {
            return needsHelicopter;
        }

        public PriorityLevel getPriorityLevel() {
            return priorityLevel;
        }

        @Override
        public String toString() {
            return arrivalOrder + ". " + name +
                    " | " + priorityLevel +
                    " | " + priorityLevel.getDescription() +
                    " | Can walk: " + canWalk +
                    " | Needs helicopter: " + needsHelicopter;
        }
    }

    public static class TriageSystem {
        private final List<Patient> patients;
        private int nextArrivalNumber;

        public TriageSystem() {
            patients = new ArrayList<>();
            nextArrivalNumber = 1;
        }

        public Patient addPatient(String name, boolean critical, boolean serious,
                                  boolean stable, boolean canWalk, boolean needsHelicopter) {
            Patient patient = new Patient(
                    nextArrivalNumber,
                    name,
                    critical,
                    serious,
                    stable,
                    canWalk,
                    needsHelicopter
            );

            patients.add(patient);
            nextArrivalNumber++;
            return patient;
        }

        public void addPatientInteractive(Scanner scanner) {
            System.out.print("Enter patient name: ");
            String name = scanner.nextLine();

            boolean critical = askYesNo(scanner, "Is the patient in critical condition? (y/n): ");
            boolean serious = askYesNo(scanner, "Is the patient in serious condition? (y/n): ");
            boolean stable = askYesNo(scanner, "Is the patient stable/healthy? (y/n): ");
            boolean canWalk = askYesNo(scanner, "Can the patient walk? (y/n): ");
            boolean needsHelicopter = askYesNo(scanner, "Does the patient need helicopter evacuation? (y/n): ");

            Patient patient = addPatient(name, critical, serious, stable, canWalk, needsHelicopter);

            System.out.println("Patient added successfully.");
            System.out.println(patient);
        }

        public List<Patient> getAllPatients() {
            return new ArrayList<>(patients);
        }

        public List<Patient> getEvacuationQueue() {
            List<Patient> evacuationList = new ArrayList<>();

            for (Patient patient : patients) {
                if (patient.needsHelicopter()) {
                    evacuationList.add(patient);
                }
            }

            evacuationList.sort(
                    Comparator.comparingInt((Patient p) -> p.getPriorityLevel().getLevel())
                            .thenComparingInt(Patient::getArrivalOrder)
            );

            return evacuationList;
        }

        public List<Patient> loadHelicopterAndReturnBoarded(int capacity) {
            List<Patient> evacuationQueue = getEvacuationQueue();
            List<Patient> boarded = new ArrayList<>();

            for (int i = 0; i < evacuationQueue.size() && i < capacity; i++) {
                boarded.add(evacuationQueue.get(i));
            }

            patients.removeAll(boarded);
            return boarded;
        }

        public void displayAllPatients() {
            if (patients.isEmpty()) {
                System.out.println("No patients in the system.");
                return;
            }

            System.out.println("\n--- All Patients ---");
            for (Patient patient : patients) {
                System.out.println(patient);
            }
        }

        public void displayEvacuationQueue() {
            List<Patient> evacuationQueue = getEvacuationQueue();

            System.out.println("\n--- Helicopter Evacuation Queue ---");
            if (evacuationQueue.isEmpty()) {
                System.out.println("No patients currently need helicopter evacuation.");
                return;
            }

            for (Patient patient : evacuationQueue) {
                System.out.println(patient);
            }
        }

        public void loadHelicopterInteractive(int capacity) {
            List<Patient> boarded = loadHelicopterAndReturnBoarded(capacity);

            if (boarded.isEmpty()) {
                System.out.println("No patients need helicopter evacuation.");
                return;
            }

            System.out.println("\n--- Patients Loaded on Helicopter ---");
            for (Patient patient : boarded) {
                System.out.println(patient);
            }

            System.out.println("Helicopter departed with " + boarded.size() + " patient(s).");
        }

        private boolean askYesNo(Scanner scanner, String question) {
            while (true) {
                System.out.print(question);
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("y") || input.equals("yes")) {
                    return true;
                } else if (input.equals("n") || input.equals("no")) {
                    return false;
                } else {
                    System.out.println("Please enter y or n.");
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TriageSystem system = new TriageSystem();

        int helicopterCapacity = readCapacity(scanner);

        boolean running = true;

        while (running) {
            System.out.println("\n--- Wildfire Helicopter Triage Menu ---");
            System.out.println("1. Add patient");
            System.out.println("2. View all patients");
            System.out.println("3. View helicopter evacuation queue");
            System.out.println("4. Load helicopter");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    system.addPatientInteractive(scanner);
                    break;
                case "2":
                    system.displayAllPatients();
                    break;
                case "3":
                    system.displayEvacuationQueue();
                    break;
                case "4":
                    system.loadHelicopterInteractive(helicopterCapacity);
                    break;
                case "5":
                    running = false;
                    System.out.println("Program ended.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }

    private static int readCapacity(Scanner scanner) {
        while (true) {
            System.out.print("Enter helicopter capacity: ");
            String input = scanner.nextLine().trim();

            try {
                int capacity = Integer.parseInt(input);
                if (capacity <= 0) {
                    System.out.println("Capacity must be greater than 0.");
                } else {
                    return capacity;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }
}