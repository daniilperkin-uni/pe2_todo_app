import { createRouter, createWebHashHistory  } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      // The app is served at the root URL, e.g. http://localhost/; without
      // this redirect the shell rendered without any view.
      path: '/',
      redirect: '/todos'
    },
    {
      path: '/assignees',
      name: 'assignees',
      component: () => import('../views/AssigneesView.vue')
    },
    {
      path: '/create-assignee',
      name: 'create-assignee',
      component: () => import('../views/CreateAssigneeView.vue')
    },
    {
      path: '/assignees/:id',
      name: 'assignee-details',
      component: () => import('../views/AssigneeDetailsView.vue'),
      props: true
    },
    {
      path: '/assignees/:id/edit',
      name: 'edit-assignee',
      component: () => import('../views/AssigneeDetailsView.vue'),
      props: route => ({ ...route.params, isEditing: true })
    },
    {
      path: '/todos',
      name: 'todos',
      component: () => import('../views/TodosView.vue')
    },
    {
      path: '/board',
      name: 'board',
      component: () => import('../views/BoardView.vue')
    },
    {
      path: '/stats',
      name: 'stats',
      component: () => import('../views/TodoStatsView.vue')
    },
    {
      path: '/todos/create',
      name: 'create-todo',
      component: () => import('../views/CreateUpdateTodoView.vue')
    },
    {
      path: '/todos/:id/edit',
      name: 'edit-todo',
      component: () => import('../views/CreateUpdateTodoView.vue'),
      props: true
    },
    {
      // Unknown URLs (a typo or an old bookmark) used to render an empty
      // shell; show an explicit not-found page instead.
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue')
    }
  ]
})

export default router
