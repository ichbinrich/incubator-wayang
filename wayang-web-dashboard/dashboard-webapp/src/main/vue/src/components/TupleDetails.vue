<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->
<template>
    <div class="col-md-9 mt-4 py-2" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px">
        <h6>Select Tuples</h6>
        <div class="card-body">
            <!-- Dropdown for selecting the number of tuples -->
            <select v-model="selectedTupleCount" @change="selectTuples">
                <option value="5">5 Tuples</option>
                <option value="10">10 Tuples</option>
                <option value="25">20 Tuples</option>
                <option value="25">50 Tuples</option>
            </select>
        </div>
    </div>

    <table class="table">
        <thead>
            <tr>
                <th>
                <th>Tuple ID</th>
                </th>
                <th v-for="attribute in attributes" :key="attribute">
                    {{ attribute }}
                </th>
            </tr>
        </thead>
        <tbody>
            <th v-for="attribute in attributes" :key="attribute"></th>
            <tr v-for="tuple in selectedTuples" :key="tuple.hackit_tuple.metadata.tuple_id">
                <td>
                    <div style="display: flex; align-items: center">
                        <span style="margin-right: 10px; font-size: 14px">{{ tuple.hackit_tuple.metadata.tuple_id }}</span>

                        <button @click="toggleEditRow(tuple)" class="btn btn-secondary small-button px-1 py-1 mr-3"
                            title="Edit" v-if="tuple.hackit_tuple.metadata.tags.includes('DEBUG')">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button @click="saveRow(tuple)" class="btn btn-primary small-button px-1 py-1 mr-3"
                            v-if="isEditing(tuple)" title="Save">
                            Save
                        </button>
                        <i v-if="tuple.hackit_tuple.metadata.tags.includes('DEBUG')"
                            class="fas fa-bug red-icon text-danger small-button px-1 py-1 mr-3" title="Debug"
                            @click="debugTuple(tuple)"></i>
                    </div>
                </td>
                <td v-for="(attribute, index) in attributes" :key="attribute" class="editable-cell px-1 py-1">
                    <div v-if="isEditing(tuple)">
                        <input v-model="editedData[tuple.hackit_tuple.metadata.tuple_id][attribute]
                            " class="form-control w-100 px-1 py-1" />
                    </div>
                    <div v-else>
                        <div class="column-value">
                            {{
                                isEditing(tuple)
                                ? editedData[tuple.hackit_tuple.metadata.tuple_id][attribute]
                                : getTupleAttribute(tuple, attribute)
                            }}
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</template>
<script>
import Tuple from "./Tuple.vue";

export default {
    name: "TupleDetails",
    components: {
        Tuple,
    },
    props: {
        jobId: {
            type: String,
            required: true,
        },
        taskId: {
            type: String,
            required: true,
        },
        hackitAction: {
            type: String,
            required: false,
        },
    },
    data() {
        return {
            isLoading: true,
            tuples: [],
            selectedTuples: [],
            attributes: [
                "activity_label",
                "timestamp",
                "acceleration_x",
                "acceleration_y",
                "acceleration_z",
                "gyroscope_x",
            ],
            snackBar: {
                show: false,
                message: "",
            },
            editedData: {},
            tuplesToDisplay: [],
            selectedTupleCount: 5, // Initialize with 5 tuples
        };
    },

    created() {
        this.fetchTuples();
        this.selectedColumns = this.attributes;

        // Add this code to set selectedTuples initially
        if (this.tuples.length > 0) {
            this.selectedTuples = this.tuples.slice(0, this.selectedTupleCount);
        }
        this.selectTuples(); // Call selectTuples after fetching the tuples.
    },

    watch: {
        taskId: "filterTuples",
        hackitAction: "hackitActionRun",
    },
    computed: {
        otherTuples() {
            return this.tuples.filter(
                (tuple) => tuple.hackit_tuple.metadata.tuple_id !== "1"
            );
        },
    },
    mounted() {
        this.selectTuples(); // Call selectTuples when the component is mounted
    },

    methods: {
        async fetchTuples() {
            try {
                // Fetch Tuple ID 1 from a different endpoint and keep it in its row
                if (this.selectedTask === "1") {
                    const response = await fetch(
                        `http://localhost:3000/tuples/endpoint-for-tuple1?job_id=${this.jobId}`
                    );
                    this.tuples = [await response.json()];
                } else {
                    // Fetch other Tuple IDs from the original endpoint
                    const response = await fetch(
                        `http://localhost:3000/tuples/?job_id=${this.jobId}`
                    );
                    const otherTuples = await response.json();

                    // Filter out Tuple ID 1 from the original data
                    this.tuples = otherTuples.filter(
                        (tuple) => tuple.hackit_tuple.metadata.tuple_id !== "1"
                    );

                    // Set selectedTuples with the initial 5 tuples
                    this.selectedTuples = this.tuples.slice(0, this.selectedTupleCount);
                }
            } catch (error) {
                console.error("Error fetching tuples:", error);
            } finally {
                this.isLoading = false;
            }
        },

        selectTuples() {
            const count = parseInt(this.selectedTupleCount);
            this.selectedTuples = this.tuples.slice(0, count);
        },

        shouldDisplayTuple(tuple) {
            // Function to determine if a tuple should be displayed
            return (
                tuple.hackit_tuple.metadata.tuple_id !== "1" &&
                this.tuplesToDisplay.includes(tuple)
            );
        },

        getValueForColumn(tupleId, column) {
            // Find the first tuple with the specified ID and return the value for the given column
            const tuple = this.tuples.find(
                (t) => t.hackit_tuple.metadata.tuple_id === tupleId
            );
            if (tuple) {
                return tuple.hackit_tuple.wayang_tuple[column] || "-";
            }
            return "-";
        },

        addTestTuples() {
            for (let i = 0; i < 50; i++) {
                const testTuple = {
                    hackit_tuple: {
                        metadata: {
                            tuple_id: `TestTuple${i}`,
                            tags: ["testTag1", "testTag2"],
                        },
                    },
                };
                this.tuples.push(testTuple);
            }
        },

        filterTuples() {
            this.selectedTask = this.taskId;
        },

        editRow(tuple) {
            if (!this.editedData[tuple.hackit_tuple.metadata.tuple_id]) {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
            }

            // Initialize edited data with current values
            this.selectedColumns.forEach((column) => {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id][column] =
                    this.getValueForColumn(tuple.hackit_tuple.metadata.tuple_id, column);
            });
        },

        isEditing(tuple) {
            return (
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] &&
                Object.keys(this.editedData[tuple.hackit_tuple.metadata.tuple_id])
                    .length > 0
            );
        },

        saveCell(tupleId, attribute) {
            const tuple = this.tuples.find(
                (t) => t.hackit_tuple.metadata.tuple_id === tupleId
            );

            if (
                tuple &&
                this.editedData[tupleId] &&
                this.editedData[tupleId][attribute] !== undefined
            ) {
                tuple.hackit_tuple.wayang_tuple[attribute] =
                    this.editedData[tupleId][attribute];
            }
        },

        saveChanges(tupleId, colIndex) {
            if (this.editedData[tupleId]) {
                const editedValue = this.editedData[tupleId][colIndex];
                // Implement a way to save the edited value on the server-side
                // For now, we'll just update the local data
                const tuple = this.tuples.find(
                    (t) => t.hackit_tuple.metadata.tuple_id === tupleId
                );
                if (tuple) {
                    tuple.hackit_tuple.wayang_tuple[this.selectedColumns[colIndex]] =
                        editedValue;
                }
                // Clear the edited data
                this.editedData[tupleId][colIndex] = undefined;
            }
        },

        debugTuple(tuple) {
            if (this.areAnyInputsEmpty(tuple)) {
                this.showSnackBarMessage("Debug incomplete, edit the columns.");
                return;
            }
            try {
                // Perform debugging actions here for the 'tuple' object
                // You can access the 'tuple' data and perform debugging operations.
                this.showSnackBarMessage("Tuple debugged");
            } catch (error) {
                this.showSnackBarMessage("Error debugging the tuple");
            }
        },

        areAnyInputsEmpty(tuple) {
            return (
                tuple.hackit_tuple.metadata.isBug &&
                Array.from({ length: 5 }, (_, i) => i + 1)
                    .map(
                        (num) =>
                            this.$refs[
                            "inputField" + num + "_" + tuple.hackit_tuple.metadata.tuple_id
                            ][0]
                    )
                    .some((input) => !input.value)
            );
        },

        toggleEditRow(tuple) {
            if (
                !this.isEditing(tuple) &&
                tuple.hackit_tuple.metadata.tags.includes("DEBUG")
            ) {
                // Enable editing for this row
                this.enableEditRow(tuple);
            } else {
                // Disable editing for this row
                this.disableEditRow(tuple);
            }
        },

        editRow(tuple) {
            if (!this.editedData[tuple.hackit_tuple.metadata.tuple_id]) {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
            }

            // Initialize edited data with current values
            this.selectedColumns.forEach((column) => {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id][column] =
                    this.getValueForColumn(tuple.hackit_tuple.metadata.tuple_id, column);
            });

            this.editingRow = tuple; // Mark the row as currently editing
        },

        saveRow(tuple) {
            const tupleId = tuple.hackit_tuple.metadata.tuple_id;

            if (this.editedData[tupleId]) {
                // Implement a way to save the edited values on the server-side
                // For now, we'll just update the local data
                this.selectedColumns.forEach((column) => {
                    const editedValue = this.editedData[tupleId][column];
                    const tuple = this.tuples.find(
                        (t) => t.hackit_tuple.metadata.tuple_id === tupleId
                    );
                    if (tuple) {
                        tuple.hackit_tuple.wayang_tuple[column] = editedValue;
                    }
                });

                // Clear the edited data for this tuple
                this.editedData[tupleId] = {};

                // Set the editingRow to null to exit edit mode
                this.editingRow = null;

                // Show a success message
                this.showSnackBarMessage(`Tuple ${tupleId} saved successfully.`);
            }
        },

        showSnackBarMessage(message) {
            this.snackBar.message = message;
            this.snackBar.show = true;
            setTimeout(() => {
                this.snackBar.show = false;
            }, 3000);
        },

        toggleEditRow(tuple) {
            if (!this.isEditing(tuple)) {
                this.enableEditRow(tuple);
            } else {
                this.disableEditRow(tuple);
            }
        },

        enableEditRow(tuple) {
            if (!this.editedData[tuple.hackit_tuple.metadata.tuple_id]) {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
            }

            // Initialize edited data with current values
            this.attributes.forEach((attribute) => {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id][attribute] =
                    this.getTupleAttribute(tuple, attribute);
            });
        },

        disableEditRow(tuple) {
            // Clear the edited data for this row
            this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
        },

        getTupleAttribute(tuple, attribute) {
            if (tuple && tuple.hackit_tuple && tuple.hackit_tuple.wayang_tuple) {
                const value = tuple.hackit_tuple.wayang_tuple[attribute];
                return value !== undefined ? value : "-";
            }
            return "-";
        },
    },
};
</script>
<style scoped></style>
