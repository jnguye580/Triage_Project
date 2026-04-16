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
        private final int resourceNeeded;

        public Patient(int arrivalOrder, String name, boolean criticalCondition,
                       boolean seriousCondition, boolean stableCondition,
                       boolean canWalk, boolean needsHelicopter,
                       PriorityLevel priorityLevel, int resourceNeeded) {
            this.arrivalOrder = arrivalOrder;
            this.name = name;
            this.criticalCondition = criticalCondition;
            this.seriousCondition = seriousCondition;
            this.stableCondition = stableCondition;
            this.canWalk = canWalk;
            this.needsHelicopter = needsHelicopter;
            this.priorityLevel = priorityLevel;
            this.resourceNeeded = resourceNeeded;
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

        public int getResourceNeeded() {
            return resourceNeeded;
        }

        @Override
        public String toString() {
            return arrivalOrder + ". " + name +
                    " | " + priorityLevel +
                    " | " + priorityLevel.getDescription() +
                    " | Resources needed: " + resourceNeeded +
                    " | Can walk: " + canWalk +
                    " | Needs helicopter: " + needsHelicopter;
        }
    }

    public static class LoadResult {
        private final List<Patient> boardedPatients;
        private final List<Patient> waitingForResources;
        private final int resourcesBefore;
        private final int resourcesAfter;

        public LoadResult(List<Patient> boardedPatients, List<Patient> waitingForResources,
                          int resourcesBefore, int resourcesAfter) {
            this.boardedPatients = new ArrayList<>(boardedPatients);
            this.waitingForResources = new ArrayList<>(waitingForResources);
            this.resourcesBefore = resourcesBefore;
            this.resourcesAfter = resourcesAfter;
        }

        public List<Patient> getBoardedPatients() {
            return new ArrayList<>(boardedPatients);
        }

        public List<Patient> getWaitingForResources() {
            return new ArrayList<>(waitingForResources);
        }

        public int getResourcesBefore() {
            return resourcesBefore;
        }

        public int getResourcesAfter() {
            return resourcesAfter;
        }
    }

    public static class TriageSystem {
        private final List<Patient> patients;
        private int nextArrivalNumber;
        private int availableResources;
        private final int priority1ResourceCost;
        private final int priority2ResourceCost;
        private final int priority3ResourceCost;

        public TriageSystem() {
            this(10, 3, 2, 1);
        }

        public TriageSystem(int availableResources, int priority1ResourceCost,
                            int priority2ResourceCost, int priority3ResourceCost) {
            if (availableResources < 0) {
                throw new IllegalArgumentException("Available resources cannot be negative.");
            }
            if (priority1ResourceCost <= 0 || priority2ResourceCost <= 0 || priority3ResourceCost <= 0) {
                throw new IllegalArgumentException("Resource costs must be greater than 0.");
            }

            this.patients = new ArrayList<>();
            this.nextArrivalNumber = 1;
            this.availableResources = availableResources;
            this.priority1ResourceCost = priority1ResourceCost;
            this.priority2ResourceCost = priority2ResourceCost;
            this.priority3ResourceCost = priority3ResourceCost;
        }

        public Patient addPatient(String name, boolean critical, boolean serious,
                                  boolean stable, boolean canWalk, boolean needsHelicopter) {
            validateConditionFlags(critical, serious, stable);

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Patient name cannot be empty.");
            }

            PriorityLevel priority = determinePriority(critical, serious, stable);
            int resourceNeeded = getResourceCostForPriority(priority);

            Patient patient = new Patient(
                    nextArrivalNumber,
                    name.trim(),
                    critical,
                    serious,
                    stable,
                    canWalk,
                    needsHelicopter,
                    priority,
                    resourceNeeded
            );

            patients.add(patient);
            nextArrivalNumber++;
            return patient;
        }

        public void addPatientInteractive(Scanner scanner) {
            String name = readName(scanner);
            int conditionChoice = readConditionChoice(scanner);

            boolean critical = conditionChoice == 1;
            boolean serious = conditionChoice == 2;
            boolean stable = conditionChoice == 3;

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

        public LoadResult loadHelicopter(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("Helicopter capacity must be greater than 0.");
            }

            List<Patient> evacuationQueue = getEvacuationQueue();
            List<Patient> boarded = new ArrayList<>();
            List<Patient> waitingForResources = new ArrayList<>();
            int resourcesBefore = availableResources;

            for (Patient patient : evacuationQueue) {
                if (boarded.size() >= capacity) {
                    break;
                }

                if (patient.getResourceNeeded() <= availableResources) {
                    boarded.add(patient);
                    availableResources -= patient.getResourceNeeded();
                } else {
                    waitingForResources.add(patient);
                    break; // strict priority: stop if the next highest-priority patient cannot be supported
                }
            }

            patients.removeAll(boarded);

            return new LoadResult(boarded, waitingForResources, resourcesBefore, availableResources);
        }

        public List<Patient> loadHelicopterAndReturnBoarded(int capacity) {
            return loadHelicopter(capacity).getBoardedPatients();
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
            LoadResult result = loadHelicopter(capacity);

            System.out.println("\n--- Helicopter Loading ---");
            System.out.println("Resources before loading: " + result.getResourcesBefore());

            List<Patient> boarded = result.getBoardedPatients();
            List<Patient> waitingForResources = result.getWaitingForResources();

            if (boarded.isEmpty()) {
                if (!waitingForResources.isEmpty()) {
                    Patient blocked = waitingForResources.get(0);
                    System.out.println("No patients were loaded.");
                    System.out.println("The next highest-priority patient is " + blocked.getName()
                            + " and requires " + blocked.getResourceNeeded()
                            + " resources, but only " + result.getResourcesAfter() + " remain.");
                } else {
                    System.out.println("No patients need helicopter evacuation.");
                }

                System.out.println("Resources remaining: " + result.getResourcesAfter());
                return;
            }

            System.out.println("Patients loaded on helicopter:");
            for (Patient patient : boarded) {
                System.out.println(patient);
            }

            if (!waitingForResources.isEmpty()) {
                Patient blocked = waitingForResources.get(0);
                System.out.println("Loading stopped because the next highest-priority patient is "
                        + blocked.getName() + " and requires " + blocked.getResourceNeeded()
                        + " resources, but only " + result.getResourcesAfter() + " remain.");
            }

            System.out.println("Helicopter departed with " + boarded.size() + " patient(s).");
            System.out.println("Resources remaining: " + result.getResourcesAfter());
        }

        public void displayResourceStatus() {
            System.out.println("\n--- Resource Status ---");
            System.out.println("Available resources: " + availableResources);
            System.out.println("PRIORITY_1 cost: " + priority1ResourceCost);
            System.out.println("PRIORITY_2 cost: " + priority2ResourceCost);
            System.out.println("PRIORITY_3 cost: " + priority3ResourceCost);
        }

        public int getAvailableResources() {
            return availableResources;
        }

        public void addResources(int amount) {
            if (amount <= 0) {
                throw new IllegalArgumentException("Resource amount must be greater than 0.");
            }
            availableResources += amount;
        }

        public void addResourcesInteractive(Scanner scanner) {
            int amount = readPositiveInt(scanner, "Enter number of resources to add: ");
            addResources(amount);
            System.out.println("Resources added successfully.");
            System.out.println("Available resources: " + availableResources);
        }

        private void validateConditionFlags(boolean critical, boolean serious, boolean stable) {
            int count = 0;
            if (critical) count++;
            if (serious) count++;
            if (stable) count++;

            if (count != 1) {
                throw new IllegalArgumentException(
                        "Exactly one condition must be selected: critical, serious, or stable."
                );
            }
        }

        private PriorityLevel determinePriority(boolean critical, boolean serious, boolean stable) {
            if (critical) {
                return PriorityLevel.PRIORITY_1;
            } else if (serious) {
                return PriorityLevel.PRIORITY_2;
            } else {
                return PriorityLevel.PRIORITY_3;
            }
        }

        private int getResourceCostForPriority(PriorityLevel priority) {
            switch (priority) {
                case PRIORITY_1:
                    return priority1ResourceCost;
                case PRIORITY_2:
                    return priority2ResourceCost;
                default:
                    return priority3ResourceCost;
            }
        }

        private String readName(Scanner scanner) {
            while (true) {
                System.out.print("Enter patient name: ");
                String name = scanner.nextLine().trim();

                if (!name.isEmpty()) {
                    return name;
                }

                System.out.println("Name cannot be empty.");
            }
        }

        private int readConditionChoice(Scanner scanner) {
            while (true) {
                System.out.println("Select patient condition:");
                System.out.println("1. Critical");
                System.out.println("2. Serious");
                System.out.println("3. Stable");
                System.out.print("Choose 1, 2, or 3: ");

                String input = scanner.nextLine().trim();

                if (input.equals("1") || input.equals("2") || input.equals("3")) {
                    return Integer.parseInt(input);
                }

                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
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

        private int readPositiveInt(Scanner scanner, String prompt) {
            while (true) {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                try {
                    int value = Integer.parseInt(input);
                    if (value > 0) {
                        return value;
                    }
                    System.out.println("Please enter a number greater than 0.");
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid whole number.");
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int helicopterCapacity = readPositiveInt(scanner, "Enter helicopter capacity: ");
        int totalResources = readPositiveInt(scanner, "Enter total available resources: ");
        int[] resourceCosts = readResourceCosts(scanner);

        TriageSystem system = new TriageSystem(
                totalResources,
                resourceCosts[0],
                resourceCosts[1],
                resourceCosts[2]
        );

        boolean running = true;

        while (running) {
            System.out.println("\n--- Wildfire Helicopter Triage Menu ---");
            System.out.println("1. Add patient");
            System.out.println("2. View all patients");
            System.out.println("3. View helicopter evacuation queue");
            System.out.println("4. Load helicopter");
            System.out.println("5. View resource status");
            System.out.println("6. Add more resources");
            System.out.println("7. Exit");
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
                    system.displayResourceStatus();
                    break;
                case "6":
                    system.addResourcesInteractive(scanner);
                    break;
                case "7":
                    running = false;
                    System.out.println("Program ended.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }

        scanner.close();
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Please enter a number greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static int[] readResourceCosts(Scanner scanner) {
        while (true) {
            int priority1Cost = readPositiveInt(scanner, "Enter resources needed for PRIORITY_1 patients: ");
            int priority2Cost = readPositiveInt(scanner, "Enter resources needed for PRIORITY_2 patients: ");
            int priority3Cost = readPositiveInt(scanner, "Enter resources needed for PRIORITY_3 patients: ");

            if (priority1Cost >= priority2Cost && priority2Cost >= priority3Cost) {
                return new int[] { priority1Cost, priority2Cost, priority3Cost };
            }

            System.out.println("Please use values where PRIORITY_1 >= PRIORITY_2 >= PRIORITY_3.");
        }
    }
}