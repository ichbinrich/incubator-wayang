import { createApp } from 'vue';

const app = createApp({});
const eventBus = app.config.globalProperties.$eventBus = createApp({});
export default eventBus;
