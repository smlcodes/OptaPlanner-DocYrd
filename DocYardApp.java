

package com.dpworld;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.dpworld.domain.Truck;
import com.dpworld.domain.DocYard;
import com.dpworld.domain.DocYardPlanningSolution;
import com.dpworld.domain.Timeslot;
import com.dpworld.solver.DocYardConstraintProvider;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocYardApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocYardApp.class);

    public static void main(String[] args) {
        SolverFactory<DocYardPlanningSolution> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(DocYardPlanningSolution.class)
                .withEntityClasses(Truck.class)
                .withConstraintProviderClass(DocYardConstraintProvider.class)
                // The solver runs only for 5 seconds on this small dataset.
                // It's recommended to run for at least 5 minutes ("5m") otherwise.
                .withTerminationSpentLimit(Duration.ofSeconds(10)));

        // Load the problem
        DocYardPlanningSolution problem = generateDemoData();

        // Solve the problem
        Solver<DocYardPlanningSolution> solver = solverFactory.buildSolver();
        DocYardPlanningSolution solution = solver.solve(problem);

        // Visualize the solution
        printDocYardPlaning(solution);
    }

    public static DocYardPlanningSolution generateDemoData() {
        List<Timeslot> timeslotList = new ArrayList<>(10);
        timeslotList.add(new Timeslot(LocalTime.of(8, 00), LocalTime.of(9, 00)));
        timeslotList.add(new Timeslot(LocalTime.of(9, 00), LocalTime.of(10, 00)));
        timeslotList.add(new Timeslot(LocalTime.of(10, 00), LocalTime.of(11, 00)));
        List<DocYard> docYardList = new ArrayList<>(3);
        docYardList.add(new DocYard("DocYard-A-40", 40));
        docYardList.add(new DocYard("DocYard-B-20", 20));
        docYardList.add(new DocYard("DocYard-C-10", 10));

        List<Truck> truckList = new ArrayList<>();
        long id = 0;

        truckList.add(new Truck(id++, "T10", 10));
        truckList.add(new Truck(id++, "T15", 15));
        truckList.add(new Truck(id++, "T05", 5));
        truckList.add(new Truck(id++, "T30", 30));
        truckList.add(new Truck(id++, "T40", 40));
        truckList.add(new Truck(id++, "T20", 20));
        truckList.add(new Truck(id++, "T03", 3));
        truckList.add(new Truck(id++, "T19", 19));
        truckList.add(new Truck(id++, "T22", 22));


        truckList.add(new Truck(id++, "T11", 11));
        truckList.add(new Truck(id++, "T12", 12));



        return new DocYardPlanningSolution(timeslotList, docYardList, truckList);
    }

    private static void printDocYardPlaning(DocYardPlanningSolution planningSolution) {
        LOGGER.info("");
        List<DocYard> docYardList = planningSolution.getDocYardList();
        List<Truck> truckList = planningSolution.getTruckList();

        Map<Timeslot, Map<DocYard, List<Truck>>> truckMap = truckList.stream()
                .filter(truck -> truck.getTimeslot() != null && truck.getDocYard() != null)
                .collect(Collectors.groupingBy(Truck::getTimeslot, Collectors.groupingBy(Truck::getDocYard)));
        LOGGER.info("|            | " + docYardList.stream()
                .map(docYard -> String.format("%-10s", docYard.getName())).collect(Collectors.joining(" | ")) + " |");
        LOGGER.info("|" + "------------|".repeat(docYardList.size() + 1));
        for (Timeslot timeslot : planningSolution.getTimeslotList()) {
            List<List<Truck>> cellList = docYardList.stream()
                    .map(room -> {
                        Map<DocYard, List<Truck>> byRoomMap = truckMap.get(timeslot);
                        if (byRoomMap == null) {
                            return Collections.<Truck>emptyList();
                        }
                        List<Truck> cellTruckList = byRoomMap.get(room);
                        if (cellTruckList == null) {
                            return Collections.<Truck>emptyList();
                        }
                        return cellTruckList;
                    })
                    .collect(Collectors.toList());

            LOGGER.info("| " + String.format("%-10s",
                   timeslot.getStartTime()) + " | "
                    + cellList.stream().map(cellTruckList -> String.format("%-10s",
                            cellTruckList.stream().map(Truck::getTruckName).collect(Collectors.joining(","))))
                            .collect(Collectors.joining(" | "))
                    + " |");

            LOGGER.info("|" + "------------|".repeat(docYardList.size() + 1));
        }
        List<Truck> unassignedTrucks = truckList.stream()
                .filter(truck -> truck.getTimeslot() == null || truck.getDocYard() == null)
                .collect(Collectors.toList());
        if (!unassignedTrucks.isEmpty()) {
            LOGGER.info("");
            LOGGER.info("Unassigned trucks");
            for (Truck truck : unassignedTrucks) {
                LOGGER.info("  " + truck.getTruckName() + " - " + truck.getTruckCapacity());
            }
        }
    }

}
