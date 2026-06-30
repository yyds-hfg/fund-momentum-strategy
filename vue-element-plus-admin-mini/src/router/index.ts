import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import type { App } from 'vue'
import { Layout } from '@/utils/routerHelper'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

export const constantRouterMap: AppRouteRecordRaw[] = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    name: 'Root',
    meta: {
      hidden: true
    }
  },
  {
    path: '/redirect',
    component: Layout,
    name: 'Redirect',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFind',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  }
]

export const asyncRouterMap: AppRouteRecordRaw[] = [
  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/index',
    name: 'Dashboard',
    meta: {
      title: t('router.dashboard'),
      icon: 'ant-design:dashboard-outlined'
    },
    children: [
      {
        path: 'index',
        name: 'DashboardIndex',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: {
          title: t('router.dashboard'),
          noCache: true
        }
      }
    ]
  },
  {
    path: '/fund',
    component: Layout,
    redirect: '/fund/list',
    name: 'Fund',
    meta: {
      title: t('router.fund'),
      icon: 'ant-design:wallet-outlined'
    },
    children: [
      {
        path: 'list',
        name: 'FundList',
        component: () => import('@/views/Fund/list.vue'),
        meta: {
          title: t('router.fundList')
        }
      }
    ]
  },
  {
    path: '/strategy',
    component: Layout,
    redirect: '/strategy/config',
    name: 'Strategy',
    meta: {
      title: t('router.strategy'),
      icon: 'ant-design:line-chart-outlined'
    },
    children: [
      {
        path: 'config',
        name: 'StrategyConfig',
        component: () => import('@/views/Strategy/config.vue'),
        meta: {
          title: t('router.strategyConfig')
        }
      },
      {
        path: 'execute',
        name: 'StrategyExecute',
        component: () => import('@/views/Strategy/execute.vue'),
        meta: {
          title: t('router.strategyExecute')
        }
      }
    ]
  },
  {
    path: '/backtest',
    component: Layout,
    redirect: '/backtest/run',
    name: 'Backtest',
    meta: {
      title: t('router.backtest'),
      icon: 'ant-design:history-outlined'
    },
    children: [
      {
        path: 'run',
        name: 'BacktestRun',
        component: () => import('@/views/Backtest/run.vue'),
        meta: {
          title: t('router.backtestRun')
        }
      }
    ]
  },
  {
    path: '/report',
    component: Layout,
    redirect: '/report/weekly',
    name: 'Report',
    meta: {
      title: t('router.report'),
      icon: 'ant-design:file-text-outlined'
    },
    children: [
      {
        path: 'weekly',
        name: 'ReportWeekly',
        component: () => import('@/views/Report/weekly.vue'),
        meta: {
          title: t('router.weeklyReport')
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  strict: true,
  routes: constantRouterMap as RouteRecordRaw[],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

export const resetRouter = (): void => {
  const resetWhiteNameList = ['Redirect', 'Login', 'NoFind', 'Root']
  router.getRoutes().forEach((route) => {
    const { name } = route
    if (name && !resetWhiteNameList.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name)
    }
  })
}

export const setupRouter = (app: App<Element>) => {
  app.use(router)
}

export default router
