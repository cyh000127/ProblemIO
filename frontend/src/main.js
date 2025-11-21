import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import App from './App.vue'
import router from './router'

// PrimeVue 컴포넌트
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Textarea from 'primevue/textarea'
import Dialog from 'primevue/dialog'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import ProgressBar from 'primevue/progressbar'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Divider from 'primevue/divider'
import FileUpload from 'primevue/fileupload'
import Menubar from 'primevue/menubar'
import Chips from 'primevue/chips'
import Paginator from 'primevue/paginator'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'

// PrimeIcons
import 'primeicons/primeicons.css'

const app = createApp(App)
const pinia = createPinia()

// PrimeVue 설정
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.dark-mode',
      cssLayer: false,
    },
  },
})

app.use(ToastService)
app.use(ConfirmationService)
app.use(pinia)
app.use(router)

// PrimeVue 컴포넌트 등록
app.component('Button', Button)
app.component('Card', Card)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Textarea', Textarea)
app.component('Dialog', Dialog)
app.component('Toast', Toast)
app.component('ProgressBar', ProgressBar)
app.component('Avatar', Avatar)
app.component('Badge', Badge)
app.component('Divider', Divider)
app.component('FileUpload', FileUpload)
app.component('Menubar', Menubar)
app.component('Chips', Chips)
app.component('Paginator', Paginator)
app.component('ConfirmDialog', ConfirmDialog)

app.mount('#app')
