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
  <template id="app">
    <div class="container-fluid bg-dark">
      <div class="row align-items-center justify-content-center" style="min-height: 100px;">
        <div class="col-auto">
          <router-link to="/" class="navbar-brand d-flex justify-content-center">
            <img src="/logo.png" alt="Logo" class="logo" style="height: 400px;" />
          </router-link>
        </div>
      </div>
    </div>
    <div class="col-9">
      <router-view />
    </div>
    <footer class="text-center py-4 mt-3">
      <div class="container">
      </div>
    </footer>
  </template>
  <script>
  
  
  export default {
    name: 'App',
    computed: {
      activeRoute() {
        return this.$route.name
      }
    }
  }
  </script>
  <style scoped>
  .logo {
    max-height: 50px;
  }
  </style>
    