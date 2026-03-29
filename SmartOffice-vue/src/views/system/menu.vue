<template>
  <div class="system-menu-page">
    <div class="page-header">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="handleCreate(null)">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <!-- 菜单树 -->
    <div class="menu-tree-container">
      <el-table :data="menuList" row-key="id" :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="菜单名称" width="180" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="150" />
        <el-table-column prop="component" label="组件路径" min-width="150" />
        <el-table-column prop="menuType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="primary" size="small">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" size="small">目录</el-tag>
            <el-tag v-else type="info" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="60" />
        <el-table-column prop="visible" label="显示" width="60">
          <template #default="{ row }">
            {{ row.visible === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.parentId === 0" type="primary" link @click="handleCreate(row)">新增子菜单</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-input :value="parentMenu?.name || '顶级菜单'" disabled />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">菜单</el-radio>
            <el-radio label="C">目录</el-radio>
            <el-radio label="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.menuType !== 'F'">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 'M'">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.menuType !== 'F'">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="是否显示" v-if="form.menuType !== 'F'">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 'F'">
          <el-input v-model="form.permission" placeholder="如: system:user:add" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { getMenuList, addMenu, updateMenu, deleteMenu } from '@/api/user'

const menuList = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const parentMenu = ref<any>(null)

const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  name: '',
  path: '',
  component: '',
  menuType: 'M',
  icon: '',
  sort: 0,
  visible: 1,
  permission: ''
})

const rules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
}

// 获取菜单列表
const fetchList = async () => {
  try {
    const res = await getMenuList()
    menuList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
})

const handleCreate = (parent: any) => {
  form.id = undefined
  form.parentId = parent?.id || 0
  form.name = ''
  form.path = ''
  form.component = ''
  form.menuType = 'M'
  form.icon = ''
  form.sort = 0
  form.visible = 1
  form.permission = ''
  parentMenu.value = parent
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  parentMenu.value = { name: '顶级菜单' }
  isEdit.value = true
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜单"${row.name}"吗？`, '提示', { type: 'warning' })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 用户取消
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateMenu(form)
          ElMessage.success('更新成功')
        } else {
          await addMenu(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error(error)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.system-menu-page {
  .menu-tree-container {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 20px;
  }
}
</style>
