/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dpworld.solver;

import com.dpworld.domain.Truck;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class DocYardConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredCapacityConstraint(constraintFactory),
                docYardConflict(constraintFactory),
                truckConflict(constraintFactory)
        };
    }

    Constraint docYardConflict(ConstraintFactory constraintFactory) {
        // A room can accommodate at most one lesson at the same time.
        return constraintFactory
                // Select each pair of 2 different Trucks ...
                .forEachUniquePair(Truck.class,
                        // ... in the same timeslot ...
                        Joiners.equal(Truck::getTimeslot),
                        // ... in the same DocYard ...
                        Joiners.equal(Truck::getDocYard))
                // ... and penalize each pair with a hard weight.
                .penalize("DocYard conflict", HardSoftScore.ONE_HARD);
    }

    Constraint truckConflict(ConstraintFactory constraintFactory) {
        // A DocYard can servce at most one Truck at the same time.
        return constraintFactory
                .forEachUniquePair(Truck.class,
                        Joiners.equal(Truck::getTimeslot),
                        Joiners.equal(Truck::getTruckName))
                .penalize("Truck conflict", HardSoftScore.ONE_HARD);
    }

    Constraint requiredCapacityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Truck.class)
                .groupBy(Truck::getDocYard, Truck::getTruckCapacity)
                .filter((docYard, reqCapacity) -> reqCapacity > docYard.getCapacity())
                .penalize("requiredCapacityTotal",
                        HardSoftScore.ONE_HARD,
                        (docYard, requiredCpuPower) -> requiredCpuPower - docYard.getCapacity());
    }

}
