import './assets/main.css'

import {createApp} from 'vue'
import App from './App.vue'
import router from './router'

// fontawesome 5 icons
import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
library.add(fas);

const app = createApp(App)
app.component("FontAwesomeIcon", FontAwesomeIcon)
app.use(router)
app.mount('#app')
