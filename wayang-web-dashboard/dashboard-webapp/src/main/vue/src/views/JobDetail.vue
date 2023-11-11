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
  <div class="container-fluid margin-adjust">
    <nav class="navbar navbar-expand-lg navbar-light">
      <div class="navbar-collapse" id="navbarNavDropdown">
        <ul class="navbar-nav ml-auto">
          <!-- Dropdown for Jobs -->
          <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle bg-white text-dark" href="#" id="navbarJobDropdown" role="button"
              data-bs-toggle="dropdown" aria-expanded="false">
              Jobs
            </a>
            <ul class="dropdown-menu bg-white" aria-labelledby="navbarJobDropdown">
              <li><router-link to="/jobs/wordcount" class="dropdown-item">Wordcount</router-link></li>
              <li><router-link to="/jobs/join-query" class="dropdown-item">Join Query</router-link></li>
              <li><router-link to="/jobs/classification" class="dropdown-item">Classification</router-link></li>
              <li><router-link to="/jobs/clustering" class="dropdown-item">Clustering</router-link></li>
            </ul>
          </li>
        </ul>
      </div>
    </nav>

    <!-- Job Plan Section -->
    <div class="row justify-content-center py-3">
      <div class="col-12 col-md-10 col-lg-8 mx-auto offset-md-1">
        <div class="card mb-4">
          <div class="card-header">
            <h6>Job Plan</h6>
          </div>
          <div class="card-body">
            <h6>Right-click on the operator to select custom tags</h6>
            <JobPlan :graph="job.graph" :node-status="nodeStatus" :task_selected="task_id" :task_id="selectedTaskId"
              :code="codeContent" v-if="job.graph" @highlight-node="highlightNode" @task-selected="handleTaskSelected">
            </JobPlan>
            <div v-else class="alert alert-warning">No job plan available</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Debugger Section -->
    <div class="row justify-content-center py-3" v-if="job.hackit">
      <div class="col-12 col-md-10 col-lg-8 mx-auto offset-md-1">
        <HackitControls @next-tuple="nextTuple" :nextNodeId="nextNodeId" :codeMirrorRef="codeMirrorRef"
          @next-operator-clicked="handleNextOperatorClicked" />

      </div>
    </div>

    <!-- Tuples Card -->
    <div class="row justify-content-center py-3">
      <div class="col-12 col-md-10 col-lg-8 mx-auto offset-md-1">
        <div class="card mb-4">
          <div class="card-header">
            <h6>Debug Tuples & Tags</h6>
          </div>
          <div class="card-body">
            <HackitDebugger @update:selectedTaskId="selectedTaskId = $event" :hackitAction="hackitAction" :jobId="jobId"
              :taskId="task_id" style="max-height: 400px; overflow: auto" />
          </div>
        </div>
      </div>
    </div>

    <!-- Tuple Details -->
    <div class="row justify-content-center py-3">
      <div class="col-12 col-md-10 col-lg-8 mx-auto offset-md-1">
        <div class="card mb-4">
          <div class="card-header">
            <h6>Tuple Details</h6>
          </div>
          <div class="card-body">
            <TupleDetails :selectedTupleCountFromFirstComponent="filteredTupleCount" :hackitAction="hackitAction"
              :jobId="jobId" :taskId="selectedTaskId" />
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
import TupleDetails from "../components/TupleDetails.vue";

export default {
  name: "JobDetail",
  components: {
    HackitDebugger,
    JobPlan,
    HackitControls,
    TupleDetails,
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
      codeMirrorRef: null,
      codeContent: "",
      nextNodeId: null,
      selectedColumns: [],
      selectedTaskIds: null,
      snackBar: {
        show: false,
        message: "",
      },
      selectedTupleIndex: 0,
      selectedOperatorIndex: 0,
    };
  },

  props: {
    nodeStatus: {
      type: Object,
      default: () => ({}),
    },
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

    clearCodeMirrorContent() {
      if (this.codeMirrorRef) {
        this.codeMirrorRef.codemirror.setValue(""); // Clear the content
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
/* Add a class for your centered content */
.centered-content {
  position: relative;
  left: 100%;
  transform: translateX(-50%);
}

/* Alternatively, if you prefer to use a margin for slight adjustments */
.margin-adjust {
  margin-left: 20%;
  padding-right:10%;
  /* Adjust the percentage as necessary */
}
</style>
