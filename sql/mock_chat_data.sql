-- ============================================
-- AI对话模块测试数据
-- 数据库: so-chat
-- ============================================

USE `so-chat`;

-- 清空已有数据
TRUNCATE TABLE chat_message;
TRUNCATE TABLE chat_session;

-- ============================================
-- 插入 chat_session 数据
-- ============================================

INSERT INTO chat_session (id, user_id, title, model, status, message_count, create_time, update_time) VALUES
-- 今天
(1, 1, '年假申请流程咨询', 'qwen-plus', 0, 6, NOW(), NOW()),
(2, 1, '公司报销制度', 'qwen-plus', 0, 4, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

-- 昨天
(3, 1, '考勤制度查询', 'qwen-plus', 0, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 1, '工单处理问题', 'qwen-plus', 0, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 2天前
(5, 1, '会议室预约问题', 'qwen-plus', 0, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 3天前
(6, 1, '加班调休规定', 'qwen-plus', 0, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 5天前
(7, 1, '入职流程咨询', 'qwen-plus', 0, 4, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 7天前
(8, 1, '密码忘了怎么办', 'qwen-plus', 0, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================
-- 插入 chat_message 数据
-- ============================================

-- Session 1: 年假申请流程咨询
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(1, 'user', '我想咨询一下年假申请需要什么材料？', 'qwen-plus', 50, 1200, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 'assistant', '您好！关于年假申请，一般需要准备以下材料：\n\n1. 年假申请表（可在OA系统下载）\n2. 相关的证明文件（如有）\n3. 部门主管签字确认\n\n申请流程：\n1. 登录OA系统\n2. 进入"假勤管理"模块\n3. 选择"年假申请"\n4. 填写申请信息并提交\n\n一般需要提前3-5个工作日申请哦～', 'qwen-plus', 300, 2500, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 'user', '那我可以申请多少天年假？', 'qwen-plus', 30, 1100, DATE_SUB(NOW(), INTERVAL 2.5 HOUR)),
(1, 'assistant', '年假天数根据您的工作年限来确定：\n\n• 工作满1年不满10年：5天\n• 工作满10年不满20年：10天\n• 工作满20年及以上：15天\n\n您可以通过OA系统查看自己的年假余额。如果还有疑问，可以联系人事部门咨询～', 'qwen-plus', 280, 2300, DATE_SUB(NOW(), INTERVAL 2.5 HOUR)),
(1, 'user', '好的，谢谢！', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, 'assistant', '不客气！祝您申请顺利～如果还有其他问题，随时问我哦！😊', 'qwen-plus', 120, 1500, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- Session 2: 公司报销制度
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(2, 'user', '公司报销一般需要多久到账？', 'qwen-plus', 40, 1000, DATE_SUB(NOW(), INTERVAL 2.5 HOUR)),
(2, 'assistant', '您好！关于报销到账时间：\n\n• 普通报销：审批完成后3-5个工作日内到账\n• 加急报销：审批完成后1-2个工作日到账（需要部门负责人标注"加急"）\n\n注意：每月25日之后的报销可能需要等到下个月才能处理哦～', 'qwen-plus', 260, 2200, DATE_SUB(NOW(), INTERVAL 2.5 HOUR)),
(2, 'user', '哪些费用可以报销？', 'qwen-plus', 35, 950, DATE_SUB(NOW(), INTERVAL 2.2 HOUR)),
(2, 'assistant', '公司可报销的费用包括：\n\n1. **差旅费**：交通费、住宿费、出差补贴\n2. **业务招待费**：客户拜访时的餐费等\n3. **办公用品费**：文具、打印耗材等\n4. **交通费**：公交车、出租车费（需说明事由）\n5. **通讯费**：因公产生的电话费\n\n注意：报销时需要提供发票哦！', 'qwen-plus', 320, 2800, DATE_SUB(NOW(), INTERVAL 2.2 HOUR)),
(2, 'user', '明白了，谢谢！', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, 'assistant', '不客气！祝您工作顺利～', 'qwen-plus', 100, 1200, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- Session 3: 考勤制度查询
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(3, 'user', '公司上班时间是几点到几点？', 'qwen-plus', 45, 1100, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '公司标准上班时间：\n\n• 上午：9:00 - 12:00\n• 下午：13:30 - 18:00\n\n午休时间：12:00 - 13:30（1.5小时）\n\n弹性打卡说明：\n• 每个月有3次半小时内的迟到额度\n• 可以申请弹性工作制（需部门同意）', 'qwen-plus', 300, 2500, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user', '迟到会有什么影响吗？', 'qwen-plus', 30, 1000, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '关于迟到：\n\n1. **每月3次宽容度**：每月允许3次半小时内的迟到，不扣工资\n2. **超次处理**：超过3次后，每次扣50元\n3. **全勤奖**：当月无迟到可获得200元全勤奖\n4. **紧急情况**：如遇突发情况可申请补卡说明\n\n建议提前5-10分钟到岗，避免因交通等因素影响～', 'qwen-plus', 350, 3000, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user', '加班可以调休吗？', 'qwen-plus', 35, 1050, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '加班调休规定：\n\n1. **工作日加班**：可以申请调休或支付加班费（1.5倍工资）\n2. **周末加班**：可申请调休或支付2倍工资\n3. **节假日加班**：必须支付3倍工资，不可调休\n\n调休申请：\n• 需要提前填写加班申请单\n• 加班后15天内申请调休\n• 由部门负责人审批\n\n如有疑问可联系HR～', 'qwen-plus', 400, 3200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user', '了解了，谢谢！', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '不客气！有问题随时问我～', 'qwen-plus', 100, 1200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user', '那请假流程是什么呢？', 'qwen-plus', 40, 1100, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '请假流程：\n\n1. **普通请假**：\n   - 1天以内：直接告诉直属上级即可\n   - 3天以内：需在OA系统提交申请\n   - 3天以上：需提供证明材料\n\n2. **请假类型**：\n   - 年假、病假、事假、婚假、产假等\n\n3. **申请步骤**：\n   - 登录OA系统\n   - 进入"假勤管理"\n   - 填写请假申请\n   - 等待审批\n\n急事可以先电话告知，事后补申请～', 'qwen-plus', 420, 3500, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'user', '非常感谢！', 'qwen-plus', 15, 700, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 'assistant', '不客气！祝您工作愉快～😊', 'qwen-plus', 110, 1300, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Session 4: 工单处理问题
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(4, 'user', '工单处理超时了怎么办？', 'qwen-plus', 40, 1000, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'assistant', '工单处理超时处理方式：\n\n1. **系统自动升级**：超过24小时未处理，工单会自动升级给上级主管\n2. **催单处理**：可以在工单详情页点击"催单"按钮\n3. **转派**：如果负责人确实无法处理，可以申请转派\n\n建议：\n• 处理工单时及时更新状态\n• 无法处理时提前沟通说明\n• 复杂问题及时升级反馈', 'qwen-plus', 320, 2800, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'user', '好的，明白了', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'assistant', '好的，有问题随时咨询～', 'qwen-plus', 90, 1100, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Session 5: 会议室预约问题
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(5, 'user', '会议室怎么预约？', 'qwen-plus', 35, 900, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'assistant', '会议室预约方法：\n\n1. **OA系统预约**：\n   - 登录OA → 进入"会议室管理"\n   - 选择日期和时间段\n   - 填写会议主题和参与人数\n   - 提交等待审批\n\n2. **注意事项**：\n   - 小会议室（6人）：可提前30分钟预约\n   - 中会议室（12人）：需提前1小时预约\n   - 大会议室（30人）：需提前1天预约\n\n3. **取消预约**：\n   - 如无法参会，请提前取消释放资源\n   - 连续3次预约未使用将被限制使用权限', 'qwen-plus', 380, 3200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'user', '预约需要审批吗？', 'qwen-plus', 30, 850, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'assistant', '审批规则：\n\n• **小会议室（6人以下）**：无需审批，直接预约即可使用\n• **中会议室（6-12人）**：需部门主管审批\n• **大会议室（12人以上）**：需部门主管+行政部双重审批\n\n系统会自动检查冲突，如果预约时段已被使用会提示您选择其他时间～', 'qwen-plus', 300, 2600, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'user', '了解了，谢谢', 'qwen-plus', 20, 750, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'assistant', '不客气！祝您会议顺利～', 'qwen-plus', 100, 1200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'user', '有什么设备可以借用吗？', 'qwen-plus', 35, 950, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'assistant', '会议室可借用设备：\n\n1. **投影仪**：需提前2小时申请\n2. **白板/马克笔**：直接向行政领取\n3. **视频会议设备**：需预约时注明\n4. **无线麦克风**：向行政室借用\n\n借用流程：\n1. 在OA系统填写设备借用单\n2. 提前到行政室领取\n3. 使用完毕后归还\n\n如有设备故障请及时报修～', 'qwen-plus', 380, 3100, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'user', '好的，非常感谢！', 'qwen-plus', 25, 850, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 'assistant', '不客气！祝您工作愉快～', 'qwen-plus', 110, 1300, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Session 6: 加班调休规定
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(6, 'user', '加班费怎么计算？', 'qwen-plus', 35, 950, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'assistant', '加班费计算方式：\n\n1. **工作日加班**：\n   - 加班费 = 基本工资 ÷ 21.75 ÷ 8 × 1.5 × 加班小时数\n\n2. **周末加班**：\n   - 加班费 = 基本工资 ÷ 21.75 ÷ 8 × 2 × 加班小时数\n\n3. **法定节假日加班**：\n   - 加班费 = 基本工资 ÷ 21.75 ÷ 8 × 3 × 加班小时数\n\n例如：基本工资6000元，工作日加班2小时\n加班费 = 6000 ÷ 21.75 ÷ 8 × 1.5 × 2 ≈ 103元', 'qwen-plus', 420, 3500, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'user', '加班有时间限制吗？', 'qwen-plus', 30, 880, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'assistant', '加班时间规定：\n\n1. **每日上限**：一般不超过3小时/天\n2. **每月上限**：不超过36小时/月\n3. **特殊情况**：需要部门负责人特批\n\n**强制规定**：\n• 用人单位不得强迫或变相强迫劳动者加班\n• 员工有权拒绝超时加班（特殊情况除外）\n• 长期加班需评估身心健康\n\n如遇强制加班情况，可向HR或工会反映～', 'qwen-plus', 380, 3100, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'user', '调休和加班费可以互相转换吗？', 'qwen-plus', 40, 1050, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'assistant', '调休与加班费转换规则：\n\n1. **工作日加班**：可以选调休或加班费\n2. **周末加班**：可以选调休或加班费\n3. **节假日加班**：只能选加班费，不能调休\n\n**注意事项**：\n• 调休必须在加班后15天内申请\n• 过期未申请视同放弃（自动结算加班费）\n• 调休以小时为单位，不满1小时按1小时计算\n• 调休期间工资照常发放\n\n可以根据实际需求灵活选择～', 'qwen-plus', 400, 3300, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'user', '明白了，谢谢！', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 'assistant', '不客气！加班也要注意身体哦～', 'qwen-plus', 120, 1400, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Session 7: 入职流程咨询
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(7, 'user', '新员工入职需要准备什么？', 'qwen-plus', 40, 1000, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'assistant', '新员工入职准备材料：\n\n**必备材料**：\n1. 身份证原件及复印件2份\n2. 学历证书原件及复印件1份\n3. 离职证明（应届毕业生除外）\n4. 近期一寸白底证件照3张\n5. 社保卡复印件1份\n6. 体检报告（部分岗位需要）\n\n**入职流程**：\n1. 9:00到公司报到\n2. 签署劳动合同\n3. 办理入职手续\n4. 领取工牌、门禁卡\n5. 部门负责人交接\n\n建议提前10分钟到达公司哦～', 'qwen-plus', 420, 3600, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'user', '第一天有什么安排吗？', 'qwen-plus', 35, 950, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'assistant', '第一天入职安排：\n\n**上午**：\n• 9:00-10:00 HR入职培训\n• 10:00-11:00 IT部门领取电脑、开通账号\n• 11:00-12:00 签订合同、办理手续\n\n**下午**：\n• 13:30-14:30 部门负责人面谈\n• 14:30-16:00 了解部门同事和工作内容\n• 16:00-17:00 熟悉办公环境\n\n**温馨提示**：\n• 中午可跟随同事一起用餐\n• 有任何问题随时询问HR或同事\n• 第一天主要是熟悉环境，不用太紧张～', 'qwen-plus', 450, 3800, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'user', '公司有培训吗？', 'qwen-plus', 30, 850, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'assistant', '公司培训体系：\n\n**新员工培训**：\n• 入职培训：入职第一周\n• 岗位技能培训：入职第一个月\n• 导师制：入职后3个月有导师带教\n\n**在职培训**：\n• 技能提升培训：每月1-2次\n• 管理能力培训：管理层定期培训\n• 外派培训：优秀员工外派学习\n\n**在线学习**：\n• 公司内部学习平台\n• 各部门知识库文档\n• 行业研讨会参会机会\n\n培训费用公司全额报销哦～', 'qwen-plus', 380, 3200, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'user', '太好了，谢谢！', 'qwen-plus', 20, 800, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 'assistant', '不客气！欢迎加入我们～祝您工作愉快！😊', 'qwen-plus', 130, 1500, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Session 8: 密码忘了怎么办
INSERT INTO chat_message (session_id, role, content, model, tokens, cost_time, create_time) VALUES
(8, 'user', 'OA系统密码忘了怎么办？', 'qwen-plus', 40, 1000, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 'assistant', '密码找回方法：\n\n1. **自助找回**（推荐）：\n   - 登录页面点击"忘记密码"\n   - 输入绑定的手机号或邮箱\n   - 获取验证码后重置密码\n\n2. **联系IT重置**：\n   - 拨打IT服务热线：8001\n   - 提供姓名、工号验证身份\n   - IT人员帮你重置密码\n\n**温馨提示**：\n• 重置后请及时修改为复杂密码\n• 建议密码包含大小写字母+数字+特殊字符\n• 不要使用与其他网站相同的密码', 'qwen-plus', 380, 3100, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 'user', '好的，非常感谢！', 'qwen-plus', 25, 850, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 'assistant', '不客气！有其他问题随时联系我～', 'qwen-plus', 110, 1300, DATE_SUB(NOW(), INTERVAL 7 DAY));

-- ============================================
-- 验证数据
-- ============================================

-- 查看会话数据
SELECT '=== 会话列表 ===' AS '';
SELECT id, title, message_count, DATE_FORMAT(create_time, '%Y-%m-%d %H:%i') as create_time, DATE_FORMAT(update_time, '%Y-%m-%d %H:%i') as update_time FROM chat_session ORDER BY update_time DESC;

-- 查看消息统计
SELECT '=== 消息统计 ===' AS '';
SELECT session_id, COUNT(*) as msg_count FROM chat_message GROUP BY session_id;

-- 查看总消息数
SELECT '=== 总消息数 ===' AS '';
SELECT COUNT(*) as total_messages FROM chat_message;
