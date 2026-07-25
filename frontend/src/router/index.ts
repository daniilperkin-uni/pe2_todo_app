import { createRouter, createWebHashHistory  } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
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
      path: '/todos/create',
      name: 'create-todo',
      component: () => import('../views/CreateUpdateTodoView.vue')
    },
    {
      path: '/todos/:id/edit',
      name: 'edit-todo',
      component: () => import('../views/CreateUpdateTodoView.vue'),
      props: true
    }


  ]
})

export default router
