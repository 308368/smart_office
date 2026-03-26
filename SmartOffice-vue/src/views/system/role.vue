<template>
  <div class="system-role-page">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <!-- 角色列表 -->
    <div class="role-content">
      <div class="role-list">
        <el-table :data="roleList" @row-click="selectRole" row-class-name="role-row">
          <el-table-column prop="name" label="角色名称" />
          <el-table-column prop="code" label="角色编码" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click.stop="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 权限配置 -->
      <div class="permission-panel">
        <h3>权限配置</h3>
        <p v-if="!selectedRole" class="tip">请选择左侧角色进行权限配置</p>
        <div v-else>
          <div class="role-info">
            <span>当前角色：</span>
            <strong>{{ selectedRole.name }}</strong>
          </div>
          <el-tree
            ref="treeRef"
            :data="menuTree"
            :props="treeProps"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenus"
            :default-expand-all="true"
          />
          <el-button type="primary" @click="handleSavePermissions" style="margin-top: 16px">
            保存权限
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getRoleList, addRole, updateRole, deleteRole, getRoleMenus, assignMenus, getMenuList } from '@/api/user'

const roleList = ref<any[]>([])
const menuTree = ref<any[]>([])
const selectedRole = ref<any>(null)
const checkedMenus = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const treeRef = ref()

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const treeProps = {
  children: 'children',
  label: 'name'
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 获取菜单树
const fetchMenus = async () => {
  try {
    const res = await getMenuList()
    menuTree.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 选择角色
const selectRole = async (row: any) => {
  selectedRole.value = row
  try {
    const res = await getRoleMenus(row.id)
    checkedMenus.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

// 保存权限
const handleSavePermissions = async () => {
  if (!selectedRole.value) return
  const checkedKeys = treeRef.value?.getCheckedKeys() || []
  const halfCheckedKeys = treeRef.value?.getHalfCheckedKeys() || []
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  try {
    await assignMenus(selectedRole.value.id, allKeys)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchRoles()
  fetchMenus()
})

const handleCreate = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.description = ''
  form.status = 1
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, row)
  isEdit.value = true
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${row.name}"吗？`, '提示', { type: 'warning' })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
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
          await updateRole(form)
          ElMessage.success('修改成功')
        } else {
          await addRole(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchRoles()
      } catch (error) {
        console.error(error)
      }
    }
  })
}
</script>

<style scoped lang="scss">
.system-role-page {
  .role-content {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 20px;

    .role-list {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;
    }

    .permission-panel {
      background: #FFFFFF;
      border-radius: 12px;
      padding: 20px;

      h3 {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 16px;
      }

      .tip {
        color: #9CA3AF;
        text-align: center;
        padding: 40px 0;
      }

      .role-info {
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #E5E7EB;

        span {
          color: #6B7280;
        }

        strong {
          color: #10B981;
        }
      }
    }
  }
}
</style>
