<template>
  <div class="system-user-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新增
      </el-button>
    </div>

    <!-- 搜索 -->
    <div class="search-bar">
      <el-input v-model="searchForm.username" placeholder="搜索用户名..." clearable style="width: 200px" />
      <el-input v-model="searchForm.phone" placeholder="搜索手机号..." clearable style="width: 200px" />
      <el-button @click="fetchList">搜索</el-button>
    </div>

    <!-- 用户列表 -->
    <el-table :data="userList">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column prop="roleIds" label="角色" min-width="150">
        <template #default="{ row }">
          <el-tag v-for="roleId in row.roleIds" :key="roleId" type="info" size="small" style="margin-right: 4px">
            {{ getRoleName(roleId) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="150" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      layout="total, prev, pager, next"
      @change="fetchList"
      style="margin-top: 20px; justify-content: flex-end"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" clearable>
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" placeholder="请选择角色" multiple clearable>
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
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
import { getUserList, getUserById, getDeptList, getRoleList, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'

const userList = ref<any[]>([])
const deptList = ref<any[]>([])
const roleList = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const searchForm = reactive({ username: '', phone: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  nickname: '',
  phone: '',
  email: '',
  deptId: undefined as number | undefined,
  roleIds: [] as number[],
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const fetchList = async () => {
  try {
    const res = await getUserList({ ...searchForm, ...pagination })
    userList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchList()
  // 加载部门列表
  getDeptList().then((res: any) => {
    deptList.value = res.data || []
  })
  // 加载角色列表
  getRoleList().then((res: any) => {
    roleList.value = res.data || []
  })
})

const handleCreate = () => {
  form.id = undefined
  form.username = ''
  form.nickname = ''
  form.phone = ''
  form.email = ''
  form.deptId = undefined
  form.roleIds = []
  form.status = 1
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = async (row: any) => {
  try {
    const res = await getUserById(row.id)
    Object.assign(form, res.data)
    isEdit.value = true
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    // 用户取消
  }
}

// 根据角色ID获取角色名称
const getRoleName = (roleId: number) => {
  const role = roleList.value.find(r => r.id === roleId)
  return role ? role.name : ''
}

const handleResetPwd = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '提示', { type: 'warning' })
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
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
          await updateUser(form)
          ElMessage.success('修改成功')
        } else {
          await addUser(form)
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
.system-user-page {
  .search-bar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    background: #FFFFFF;
    padding: 16px 20px;
    border-radius: 12px;
  }

  .el-table {
    border-radius: 12px;
  }
}
</style>
