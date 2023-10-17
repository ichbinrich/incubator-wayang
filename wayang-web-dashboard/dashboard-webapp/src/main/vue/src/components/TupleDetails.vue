
<template>
    <div class="col-md-9 mt-4 py-2" style="margin-right: 10px; white-space: nowrap; padding: 5px 20px">
        <h6>Select Attributes</h6>
        <div class="card-body">
            <div v-for="(attribute, index) in attributes" :key="index" class="form-check form-check-inline">
                <input class="form-check-input" type="checkbox" :id="'attributeCheck' + index" :value="attribute"
                    v-model="selectedColumns" />
                <label class="form-check-label" :for="'attributeCheck' + index">{{ attribute }}</label>
            </div>
        </div>
    </div>

    <table class="table">
        <thead>
            <tr>
                <th></th>
                <th v-for="column in selectedColumns" :key="column">
                    {{ column }}
                </th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="tuple in otherTuples" :key="tuple.hackit_tuple.metadata.tuple_id">
                <td>
                    <div style="display: flex; align-items: center">
                        <span style="margin-right: 10px; font-size: 14px">
                            {{ tuple.hackit_tuple.metadata.tuple_id }}
                        </span>
                        <button @click="toggleEditRow(tuple)" class="btn btn-secondary small-button px-1 py-1 mr-2"
                            title="Edit">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button @click="saveRow(tuple)" class="btn btn-primary small-button px-1 py-1"
                            :disabled="!isEditing(tuple)" title="Save">
                            Save
                        </button>
                        <i v-if="tuple.hackit_tuple.metadata.tags.includes('MONITOR')" class="fas fa-magnifying-glass"
                            title="Monitor"></i>
                        <i v-if="tuple.hackit_tuple.metadata.tags.includes('DEBUG')"
                            class="fas fa-bug red-icon text-danger small-button" title="Debug"
                            @click="debugTuple(tuple)"></i>
                    </div>
                </td>
                <!-- Ensure that 'selectedColumns' contains all the required columns -->
                <td v-for="(column, colIndex) in selectedColumns" :key="column" class="editable-cell px-1 py-1">
                    <div v-if="isEditing(tuple)">
                        <input v-model="editedData[tuple.hackit_tuple.metadata.tuple_id][column]"
                            class="form-control w-100 px-1 py-1" />
                    </div>
                    <div v-else>
                        <div class="column-name">{{ }}</div>
                        <!--if removed will show the attribute's name on top -->
                        <!--div class="column-name">{{  }}</div!-->
                        <div class="column-value">
                            {{ column === 'activity_label' ? tuple.hackit_tuple.wayang_tuple.activity_label :
                                tuple.hackit_tuple.wayang_tuple[column] || '-' }}
                        </div>
                    </div>
                </td>
            </tr>
        </tbody>
    </table>
</template>  
  

<script>
import Tuple from "./Tuple.vue";
import axios from "axios";

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
            selectedColumns: [], // Initially empty, no columns are selected
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
            editedData: {}, // To store edited data
        };
    },
    created() {
        this.fetchTuples();
        this.addTestTuples();
    },
    watch: {
        taskId: function () {
            this.filterTuples();
        },
        hackitAction: function () {
            this.hackitActionRun();
        },
    },
    computed: {
        otherTuples() {
            // Filter out Tuple ID 1 from other tuples
            return this.tuples.filter(
                (tuple) => tuple.hackit_tuple.metadata.tuple_id !== "1"
            );
        },
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
                }
            } catch (error) {
                console.error("Error fetching tuples:", error);
            } finally {
                this.isLoading = false;
            }
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

        isEditing(tuple) {
            return (
                tuple.hackit_tuple.metadata.tags.includes("DEBUG") &&
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] &&
                Object.keys(this.editedData[tuple.hackit_tuple.metadata.tuple_id])
                    .length > 0
            );
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
            }
        },
        isEditing(tuple) {
            return (
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] &&
                Object.keys(this.editedData[tuple.hackit_tuple.metadata.tuple_id])
                    .length > 0
            );
        },

        editCell(tuple, colIndex) {
            if (!this.editedData[tuple.hackit_tuple.metadata.tuple_id]) {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
            }
            this.editedData[tuple.hackit_tuple.metadata.tuple_id][colIndex] =
                this.getValueForColumn(
                    tuple.hackit_tuple.metadata.tuple_id,
                    this.selectedColumns[colIndex]
                );
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

        showSnackBarMessage(message) {
            this.snackBar.message = message;
            this.snackBar.show = true;
            setTimeout(() => {
                this.snackBar.show = false;
            }, 3000);
        },
        toggleEditRow(tuple) {
            if (!this.isEditing(tuple)) {
                // Enable editing for this row
                this.enableEditRow(tuple);
            } else {
                // Disable editing for this row
                this.disableEditRow(tuple);
            }
        },

        enableEditRow(tuple) {
            if (!this.editedData[tuple.hackit_tuple.metadata.tuple_id]) {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
            }

            // Initialize edited data with current values
            this.selectedColumns.forEach((column) => {
                this.editedData[tuple.hackit_tuple.metadata.tuple_id][column] =
                    this.getValueForColumn(tuple.hackit_tuple.metadata.tuple_id, column);
            }); // <--- Add a closing parenthesis here
        },

        disableEditRow(tuple) {
            // Clear the edited data for this row
            this.editedData[tuple.hackit_tuple.metadata.tuple_id] = {};
        },
    },
};
</script>
<style scoped></style>
