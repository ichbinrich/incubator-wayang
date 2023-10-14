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
  <div class="row p-3">
    <div class="col-2 justify-content-end">
      <ul class="navbar-nav ms-auto p-3">
        <!-- Dropdown for Jobs -->
        <li class="nav-item dropdown">
          <a
            class="btn btn-outline-secondary dropdown-toggle rounded-0 border-0"
            href="#"
            role="button"
            id="dropdownMenuLink"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            :class="
              activeRoute === 'JobList'
                ? 'btn-secondary'
                : 'btn-outline-secondary'
            "
          >
            Jobs
          </a>
          <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
            <li>
              <router-link to="/jobs/wordcount" class="dropdown-item"
                >Wordcount</router-link
              >
            </li>
            <li>
              <router-link to="/jobs/join-query" class="dropdown-item"
                >Join Query</router-link
              >
            </li>
            <li>
              <router-link to="/jobs/classification" class="dropdown-item"
                >Classification</router-link
              >
            </li>
            <li>
              <router-link to="/jobs/clustering" class="dropdown-item"
                >Clustering</router-link
              >
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
  <div class="row py-1">
    <div class="col-md-3"></div>
    <div class="col-md-9">
      <!-- Job Plan Card -->
      <div class="card rounded-0">
        <div class="card-header">Job Plan</div>
        <div class="card-body">
          <h6>Right-click on the operator to select custom tags</h6>
          <JobPlan
            :graph="job.graph"
            :task_selected="task_id"
            v-if="job.graph"
            @task-selected="handleTaskSelected"
          >
          </JobPlan>
          <div class="alert alert-warning" role="alert" v-else>
            No job plan available
          </div>
        </div>
      </div>
      <div class="container mt-4">
        <div class="row">
          <div class="col-md-6"></div>
        </div>
      </div>
      <div class="col-md-6 offset-md-1 mt-3">
        <div id="hackit-debugger" v-if="job.hackit">
          <HackitControls @next-tuple="nextTuple" />
          <div class="container"></div>
          <div class="col-md-6 mt-3 mb-3"></div>
        </div>
      </div>
      <!-- Tuples Card -->
      <div class="card rounded-0 mt-3">
        <div class="card-header">Tuples</div>
        <div class="card-body tuples-card">
          <HackitDebugger
            :hackitAction="hackitAction"
            :jobId="jobId"
            :taskId="task_id"
          />
        </div>
      </div>
      <!-- Tuples details -->
      <div class="card rounded-0 mt-3">
        <div class="card-header">Tuple Details</div>
        <div
          class="col-md-9 mt-4 py-2"
          style="margin-right: 10px; white-space: nowrap; padding: 5px 20px"
        >
          <h6>Select Attributes</h6>
        </div>
        <div class="card-body">
          <div v-for="n in 5" :key="n" class="form-check form-check-inline">
            <input
              class="form-check-input"
              type="checkbox"
              :id="'columnCheck' + n"
              :value="n"
              v-model="selectedColumns"
            />
            <label class="form-check-label" :for="'columnCheck' + n"
              >Column {{ n }}</label
            >
          </div>
        </div>
        <div class="card-body">
          <table class="table">
            <thead>
              <tr>
                <th>Tuple ID</th>
                <th v-for="n in sortedSelectedColumns" :key="'header-' + n">
                  Column {{ n }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(tuple, index) in tuples"
                :key="tuple.hackit_tuple.metadata.tuple_id"
              >
                <td>
                  <span
                    :style="{
                      paddingRight: tuple.hackit_tuple.metadata.isBug
                        ? '10px'
                        : '0',
                    }"
                  >
                    {{ tuple.hackit_tuple.metadata.tuple_id }}
                  </span>
                  <button
                    v-if="tuple.hackit_tuple.metadata.isBug"
                    @click="handleDebug(index)"
                    class="btn btn-warning btn-sm"
                  >
                    Debug
                  </button>
                </td>
                <td
                  v-for="n in sortedSelectedColumns"
                  :key="
                    'data-' + n + '-' + tuple.hackit_tuple.metadata.tuple_id
                  "
                >
                  <input
                    v-if="tuple.hackit_tuple.metadata.isBug"
                    :ref="'inputField' + n + '_' + index"
                    v-model="tuple.hackit_tuple.wayang_tuple['key' + n]"
                    class="form-control form-control-sm editable-input"
                  />
                  <span v-if="!tuple.hackit_tuple.metadata.isBug"
                    >{{ tuple.hackit_tuple.wayang_tuple["key" + n] || "-" }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!--SnackBar Message-->
        <div
          class="toast"
          data-autohide="true"
          v-if="snackBar.show"
          role="alert"
          aria-live="assertive"
          aria-atomic="true"
          id="snackbarToast"
          :class="{ show: snackBar.show }"
        >
          <div class="toast-header">
            <strong class="me-auto">Notification</strong>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="toast"
              @click="snackBar.show = false"
            ></button>
          </div>
          <div class="toast-body">
            {{ snackBar.message }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import HackitDebugger from "@/components/HackitDebugger.vue";
import JobPlan from "@/components/JobPlan.vue";
import HackitControls from "@/components/HackitControls.vue";
import axios from "axios";

export default {
  name: "JobDetail",
  components: {
    HackitDebugger,
    JobPlan,
    HackitControls,
  },

  data() {
    return {
      job: {},
      jobId: this.$route.params.id,
      task_id: null,
      hackitAction: null,
      metrics: {},
      showModal: false,
      selectedNode: null,
      selectedColumns: [],
      snackBar: {
        show: false,
        message: "",
      },
      selectedTupleIndex: 0,
      selectedOperatorIndex: 0,
      tuples: [
        {
          hackit_tuple: {
            metadata: {
              //tuple_id: [],
              tuple_id: "1",
              isBug: false,
            },
            wayang_tuple: {
              key1: "Peter",
              key2: "25",
              key3: "Biology",
              key4: "Science",
              key5: "Molecular",
            },
            isEditable: false,
          },
        },
        {
          hackit_tuple: {
            metadata: {
              //tuple_id: [],
              tuple_id: "2",
              tags: [],
              isBug: false,
            },
            wayang_tuple: {
              key1: "George",
              key2: "45",
              key3: "IT",
              key4: "Technology",
              key5: "Algorithms",
            },
            isEditable: false,
          },
        },
        {
          hackit_tuple: {
            metadata: {
              //tuple_id: [],
              tuple_id: "3",
              tags: [],
              isBug: false,
            },
            wayang_tuple: {
              key1: "Maria",
              key2: "22",
              key3: "Architecture",
              key4: "Design",
              key5: "Sustainable Development",
            },
            isEditable: false,
          },
        },
        {
          hackit_tuple: {
            metadata: {
              //tuple_id: [],
              tuple_id: "4",
              //tags: ['DEBUG'],
              isBug: true,
              tags: [],
            },
            wayang_tuple: {
              key1: "Carlos",
              key2: "29",
              key3: "Med455",
              key4: "Science",
              key5: "Anatomy",
            },
            isEditable: true,
          },
        },
        {
          hackit_tuple: {
            metadata: {
              //tuple_id: [],
              tuple_id: "5",
              tags: [],
              isBug: false,
            },
            wayang_tuple: {
              key1: "value5-1",
              key2: "value5-2",
              key3: "value5-3",
              key4: "value5-4",
              key5: "value5-5",
            },
            isEditable: false,
          },
        },
      ],
    };
  },
  computed: {
    hasValidTask() {
      return this.task_id !== null;
    },
    sortedSelectedColumns() {
      return [...this.selectedColumns].sort((a, b) => a - b);
    },
  },

  mounted() {
    this.getJob(this.jobId)
      .then((data) => (this.job = data))
      .catch((error) => console.error("Error fetching job:", error));
  },
  methods: {
    async getJob(id) {
      try {
        const response = await axios(`http://localhost:3000/jobs/${id}`);
        return response.data;
      } catch (error) {
        console.error("Error fetching job details:", error);
        throw error;
      }
    },
    async fetchTuples() {
      try {
        const response = await fetch(
          `http://localhost:3000/tuples/?job_id=${this.jobId}`
        );
        const tuples = await response.json();

        // Update tuples and set isEditable based on whether a tuple is a bug
        this.tuples = tuples.map((tuple) => ({
          ...tuple,
          isEditable: tuple.hackit_tuple.metadata.isBug, // Set to true for bug tuples
        }));

        this.updateTaskIdsAndTags();
        this.filterTuples();
      } catch (error) {
        console.error("Error fetching tuples:", error);
      } finally {
        this.isLoading = false;
      }
    },

    async saveTuple(index) {
      const tuple = this.tuples[index];

      // Check if the tuple is editable (is a bug)
      if (tuple.isEditable) {
        const tupleData = {
          // Customize this data based on the structure of your backend API
          tupleId: tuple.hackit_tuple.metadata.tuple_id,
          debugData: {
            // Add properties for each column here
            key1: tuple.hackit_tuple.wayang_tuple.key1,
            key2: tuple.hackit_tuple.wayang_tuple.key2,
            // ...
          },
        };

        try {
          const response = await fetch("/path-to-backend-endpoint", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(tupleData),
          });

          if (response.ok) {
            console.log("Data saved successfully:", response.data);
          } else {
            console.error("Error saving tuple:", response.statusText);
          }
        } catch (error) {
          console.error("Error saving tuple:", error);
        }
      } else {
        console.log("This tuple is not editable (not a bug).");
      }
    },
    async saveTuple(index) {
      const tuple = this.tuples[index];

      if (tuple.isEditable) {
        const tupleData = {
          // Customize this data based on the structure of  backend API
          tupleId: tuple.hackit_tuple.metadata.tuple_id,
          debugData: {
            // Add properties for each
            key1: tuple.hackit_tuple.wayang_tuple.key1,
            key2: tuple.hackit_tuple.wayang_tuple.key2,
            key3: tuple.hackit_tuple.wayang_tuple.key3,
            key4: tuple.hackit_tuple.wayang_tuple.key4,
            key5: tuple.hackit_tuple.wayang_tuple.key5,
          },
        };

        try {
          const response = await fetch("/path-to-backend-endpoint", {
            // Add properties for each column here
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(tupleData),
          });

          if (response.ok) {
            console.log("Data saved successfully:", response.data);
          } else {
            console.error("Error saving tuple:", response.statusText);
          }
        } catch (error) {
          console.error("Error saving tuple:", error);
        }
      } else {
        console.log("This tuple is not editable (not a bug).");
      }
    },
    nextTuple() {
      this.hackitAction = "nextTuple";
    },
    handleTaskSelected(taskId) {
      this.task_id = taskId;
      console.log("Node selected:", taskId);
      this.showModal = true;
      this.openModal(taskId);
    },

    async openModal(node) {
      try {
        // GET request backend API to fetch node-specific data
        const response = await axios.get(`/api/get-node-info/${node.id}`);
        console.log("Node info:", response.data);
        this.selectedNode = response.data;

        EventBus.$emit("show-modal", true);
      } catch (error) {
        console.error("Error fetching node info:", error);
      }
      this.showModal = true;
    },
    handleEdit(event, tuple, key) {
      tuple.hackit_tuple.wayang_tuple[key] = event.target.value;
    },

    areAnyInputsEmpty(index) {
      return Array.from({ length: 5 }, (_, i) => i + 1)
        .map((num) => this.$refs["inputField" + num + "_" + index][0])
        .some((input) => !input.value);
    },

    showSnackBarMessage(message) {
      this.snackBar.message = message;
      this.snackBar.show = true;

      setTimeout(() => {
        this.snackBar.show = false;
      }, 3000);
    },
    async handleDebug(index) {
      if (this.areAnyInputsEmpty(index)) {
        this.showSnackBarMessage("Debug incomplete, edit the columns.");
        return;
      }
      try {
        await this.saveTuple(index);
        this.showSnackBarMessage("Tuple debugged");
      } catch (error) {
        this.showSnackBarMessage("Error debugging the tuple");
      }
    },

    async saveTuple(index) {
      const tupleData = Array.from({ length: 5 }, (_, i) => i + 1).reduce(
        (acc, num) => {
          acc["field" + num] =
            this.$refs["inputField" + num + "_" + index][0].value;
          return acc;
        },
        {}
      );

      try {
        const response = await axios.post(
          "/path-to-backend-endpoint",
          tupleData
        );
        console.log("Data saved successfully:", response.data);
      } catch (error) {
        console.error("Error saving tuple:", error);
        throw error;
      }
    },
    async handleAddTag() {
      try {
        const response = await axios.post("/api/add-tag", {
          nodeId: this.selectedNode.id,
          tag: "your-tag-value", // change it to the value of the tag field
        });
        console.log("Tag added:", response.data);
      } catch (error) {
        console.error("Error adding tag:", error);
      }
    },
  },
};
</script>

<style scoped>
.tuples-card {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0;
}

.editable-input {
  width: 80px;
}
</style>
