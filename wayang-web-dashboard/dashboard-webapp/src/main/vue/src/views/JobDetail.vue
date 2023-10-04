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
            id="dropdownMenuLink" data-bs-toggle="dropdown" aria-expanded="false"
            :class="activeRoute === 'JobList' ? 'btn-secondary' : 'btn-outline-secondary'">
            Jobs
          </a>
          <ul class="dropdown-menu" aria-labelledby="dropdownMenuLink">
            <li><router-link to="/jobs/wordcount" class="dropdown-item">Wordcount</router-link></li>
            <li><router-link to="/jobs/join-query" class="dropdown-item">Join Query</router-link></li>
            <li><router-link to="/jobs/classification" class="dropdown-item">Classification</router-link></li>
            <li><router-link to="/jobs/clustering" class="dropdown-item">Clustering</router-link></li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
  <div class="row py-1">
    <div class="col-md-3">
    </div>
    <div class="col-md-9">
      <!-- Job Plan Card -->
      <div class="card rounded-0">
        <div class="card-header">Job Plan</div>
        <div class="card-body">
          <h6> Right-click on the operator to select custom tags</h6>
          <JobPlan :graph="job.graph" :task_selected="task_id" v-if="job.graph" @task-selected="handleTaskSelected">
          </JobPlan>
          <div class="alert alert-warning" role="alert" v-else>
            No job plan available
          </div>
        </div>
      </div>
      <div class="container mt-4">
        <div class="row">
          <div class="col-md-6">
          </div>
        </div>
      </div>
      <div class="col-md-6 offset-md-1 mt-3">
        <div id="hackit-debugger" v-if="job.hackit">
          <HackitControls @next-tuple="nextTuple" />
          <div class="container">
          </div>
          <div class="col-md-6 mt-3 mb-3"></div>
        </div>
      </div>
      <!-- Tuples Card -->
      <div class="card rounded-0 mt-3">
        <div class="card-header">Tuples</div>
        <div class="card-body tuples-card">
          <HackitDebugger :hackitAction="hackitAction" :jobId="jobId" :taskId="task_id" />
        </div>
      </div>
      <!-- Tuples details -->
      <div class="card rounded-0 mt-3">
        <div class="card-header">Tuple Details</div>
        <div>
          <h6>Select Attributes </h6>
        </div>
        <div class="card-body">
          <div v-for="n in 5" :key="n" class="form-check form-check-inline">
            <input class="form-check-input" type="checkbox" :id="'columnCheck' + n" :value="n" v-model="selectedColumns">
            <label class="form-check-label" :for="'columnCheck' + n">Column {{ n }}</label>
          </div>
        </div>
        <div class="card-body">
          <table class="table">
            <thead>
              <tr>
                <th>Tuple ID</th>
                <th v-for="n in sortedSelectedColumns" :key="'header-' + n">Column {{ n }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(tuple, index) in tuples" :key="tuple.hackit_tuple.metadata.tuple_id">
                <td>
                  <span :style="{ 'paddingRight': tuple.hackit_tuple.metadata.isBug ? '10px' : '0' }">
                    {{ tuple.hackit_tuple.metadata.tuple_id }}
                  </span>
                  <button v-if="tuple.hackit_tuple.metadata.isBug" @click="handleDebug(index)"
                    class="btn btn-warning btn-sm">Debug</button>
                </td>
                <td v-for="n in sortedSelectedColumns" :key="'data-' + n + '-' + tuple.hackit_tuple.metadata.tuple_id">
                  <input v-if="tuple.hackit_tuple.metadata.isBug" :ref="'inputField' + n + '_' + index"
                    v-model="tuple.hackit_tuple.wayang_tuple['key' + n]"
                    class="form-control form-control-sm editable-input" />
                  <span v-if="!tuple.hackit_tuple.metadata.isBug">{{ tuple.hackit_tuple.wayang_tuple['key' + n] || '-' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!--SnackBar Message-->
        <div class="toast" data-autohide="true" v-if="snackBar.show" role="alert" aria-live="assertive" aria-atomic="true"
          id="snackbarToast" :class="{ 'show': snackBar.show }">
          <div class="toast-header">
            <strong class="me-auto">Notification</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" @click="snackBar.show = false"></button>
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
import HackitDebugger from '@/components/HackitDebugger.vue'
import JobPlan from '@/components/JobPlan.vue'
import HackitControls from '@/components/HackitControls.vue'
import axios from 'axios';


export default {
  name: 'JobDetail',
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
        message: '',
      },
      tuples: [
        {
          hackit_tuple: {
            metadata: {
              tuple_id: '12345',
            },
            wayang_tuple: {
              key1: 'value1-1',
              key2: 'value1-2',
              key3: 'value1-3',
              key4: 'value1-4',
              key5: 'value1-5'
            }
          }
        },
        {
          hackit_tuple: {
            metadata: {
              tuple_id: '67890',
              tags: []
            },
            wayang_tuple: {
              key1: 'value2-1',
              key2: 'value2-2',
              key3: 'value2-3',
              key4: 'value2-4',
              key5: 'value2-5'
            }
          }
        },
        {
          hackit_tuple: {
            metadata: {
              tuple_id: '11223',
              tags: []
            },
            wayang_tuple: {
              key1: 'value3-1',
              key2: 'value3-2',
              key3: 'value3-3',
              key4: 'value3-4',
              key5: 'value3-5'
            }
          }
        },
        {
          hackit_tuple: {
            metadata: {
              tuple_id: '44556',
              //tags: ['DEBUG'],
              isBug: true,
              tags: []
            },
            wayang_tuple: {
              key1: 'value4-1',
              key2: 'value4-2',
              key3: 'value4-3',
              key4: 'value4-4',
              key5: 'value4-5'
            }
          }
        },
        {
          hackit_tuple: {
            metadata: {
              tuple_id: '77889',
              tags: []
            },
            wayang_tuple: {
              key1: 'value5-1',
              key2: 'value5-2',
              key3: 'value5-3',
              key4: 'value5-4',
              key5: 'value5-5'
            }
          }
        }
      ],
    };
  },
  computed: {
    hasValidTask() {
      return this.task_id !== null;
    },
    sortedSelectedColumns() {
      return [...this.selectedColumns].sort((a, b) => a - b);
    }
  },

  mounted() {
    this.getJob(this.jobId)
      .then(data => this.job = data)
      .catch(error => console.error("Error fetching job:", error));
  },
  methods: {
    async getJob(id) {
      try {
        const response = await axios(`http://localhost:3000/jobs/${id}`);
        return response.data;
      } catch (error) {
        console.error('Error fetching job details:', error);
        throw error;
      }
    },
    nextTuple() {
      this.hackitAction = 'nextTuple'
    },
    handleTaskSelected(taskId) {
      this.task_id = taskId;
      console.log('Node selected:', taskId);
      this.showModal = true;
      this.openModal(taskId);

    },
    async openModal(node) {
      try {
        // GET request backend API to fetch node-specific data
        const response = await axios.get(`/api/get-node-info/${node.id}`);
        console.log('Node info:', response.data);
        this.selectedNode = response.data;

        EventBus.$emit('show-modal', true);
      } catch (error) {
        console.error('Error fetching node info:', error);
      }
      this.showModal = true;
    },
    handleEdit(event, tuple, key) {
      tuple.hackit_tuple.wayang_tuple[key] = event.target.value;
    },

    areAnyInputsEmpty(index) {
      return Array.from({ length: 5 }, (_, i) => i + 1)
        .map(num => this.$refs['inputField' + num + '_' + index][0])
        .some(input => !input.value);
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
      const tupleData = Array.from({ length: 5 }, (_, i) => i + 1)
        .reduce((acc, num) => {
          acc['field' + num] = this.$refs['inputField' + num + '_' + index][0].value;
          return acc;
        }, {});

      try {
        const response = await axios.post('/path-to-backend-endpoint', tupleData);
        console.log('Data saved successfully:', response.data);
      } catch (error) {
        console.error('Error saving tuple:', error);
        throw error;
      }
    },
    async handleAddTag() {
      try {
        const response = await axios.post('/api/add-tag', {
          nodeId: this.selectedNode.id,
          tag: 'your-tag-value', // change it to the value of the tag field
        });
        console.log('Tag added:', response.data);
      } catch (error) {
        console.error('Error adding tag:', error);
      }
    }
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
  