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
          <a class="btn btn-outline-secondary dropdown-toggle rounded-0 border-0" href="#" role="button"
            id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false" :class="activeRoute === 'JobList'
              ? 'btn-secondary'
              : 'btn-outline-secondary'
              ">
            Jobs
          </a>
          <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
            <li>
              <router-link to="/jobs/wordcount" class="dropdown-item">Wordcount</router-link>
            </li>
            <li>
              <router-link to="/jobs/join-query" class="dropdown-item">Join Query</router-link>
            </li>
            <li>
              <router-link to="/jobs/classification" class="dropdown-item">Classification</router-link>
            </li>
            <li>
              <router-link to="/jobs/clustering" class="dropdown-item">Clustering</router-link>
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
          <JobPlan :graph="job.graph" :task_selected="task_id" :task_id="selectedTaskId" :code="codeContent"
            v-if="job.graph" @highlight-node="highlightNode" @task-selected="handleTaskSelected">
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
          <HackitControls @next-tuple="nextTuple" :nextNodeId="nextNodeId" :codeMirrorRef="codeMirrorRef"
            @next-operator-clicked="handleNextOperatorClicked" />
          <div class="container"></div>
          <div class="col-md-6 mt-3 mb-3"></div>
        </div>
      </div>
      <!-- Tuples Card -->
      <div class="card rounded-0 mt-4 py-0">
        <div class="card-header">Tuples in Each Node Operator</div>
        <div class="card-body tuples-card">
          <HackitDebugger :hackitAction="hackitAction" :jobId="jobId" :taskId="task_id" />
        </div>
      </div>

      <!-- Tuples details -->
      <div class="card rounded-0 mt-4 py-0">
        <div class="card-header">Tuple Details</div>
        <div class="card-body tuples-card">
          <div v-if="job.hackit"></div>
          <TupleDetails :hackitAction="hackitAction" :jobId="jobId" :taskId="task_id" />
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
      snackBar: {
        show: false,
        message: "",
      },
      selectedTupleIndex: 0,
      selectedOperatorIndex: 0,
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
