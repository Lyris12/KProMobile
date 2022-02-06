--[[message
Universal single script to add or remove cards freely. Good to debug or test your card scripts.
]]
--created by puzzle edit
table=require("table")
io=require("io")
LOCATION_EXMZONE=128
EXILE_CARD=256
ADD_COUNTER=512
OVERLAY_CARD=1024
SAVE_FIELD=2048

card_location=LOCATION_GRAVE


custom_list={
[LOCATION_DECK]=67169062,
[LOCATION_HAND]=32807846,
[LOCATION_MZONE]=83764718,
[LOCATION_SZONE]=98494543,
[LOCATION_GRAVE]=81439173,
[LOCATION_REMOVED]=75500286,
[LOCATION_EXTRA]=24094653,
[LOCATION_EXMZONE]=61583217,
[EXILE_CARD]=15256925,
[ADD_COUNTER]=75014062,
[OVERLAY_CARD]=27068117,
[SAVE_FIELD]=11961740,
}
opcode_list={
[LOCATION_DECK]={TYPE_FUSION+TYPE_SYNCHRO+TYPE_XYZ+TYPE_LINK,OPCODE_ISTYPE,OPCODE_NOT},
[LOCATION_HAND]={TYPE_FUSION+TYPE_SYNCHRO+TYPE_XYZ+TYPE_LINK,OPCODE_ISTYPE,OPCODE_NOT},
[LOCATION_MZONE]={TYPE_MONSTER,OPCODE_ISTYPE},
[LOCATION_SZONE]={TYPE_SPELL+TYPE_TRAP+TYPE_PENDULUM,OPCODE_ISTYPE},
[LOCATION_GRAVE]=nil,
[LOCATION_REMOVED]=nil,
[LOCATION_EXTRA]={TYPE_FUSION+TYPE_SYNCHRO+TYPE_XYZ+TYPE_LINK+TYPE_PENDULUM,OPCODE_ISTYPE},
[LOCATION_EXMZONE]={TYPE_FUSION+TYPE_SYNCHRO+TYPE_XYZ+TYPE_LINK+TYPE_PENDULUM,OPCODE_ISTYPE},
}




Debug.SetAIName("ENEMY")
Debug.ReloadFieldBegin(DUEL_ATTACK_FIRST_TURN+DUEL_SIMPLE_AI,5)

local g=Group.CreateGroup()
local n=1
function sefilter(c,g)
	return not g:IsContains(c)
end
function ExileGroup(eg)
	local gg=Group.CreateGroup()
	eg:ForEach(function(tc)
		gg:Merge(tc:GetOverlayGroup())
		local e1=Effect.CreateEffect(tc)
		e1:SetType(EFFECT_TYPE_SINGLE)
		e1:SetCode(EFFECT_CANNOT_TO_HAND)
		e1:SetProperty(EFFECT_FLAG_CANNOT_DISABLE)
		e1:SetReset(RESET_EVENT+0x1fe0000)
		tc:RegisterEffect(e1,true)
		local t={EFFECT_CANNOT_TO_DECK,EFFECT_CANNOT_REMOVE,EFFECT_CANNOT_TO_GRAVE}
		for i,code in pairs(t) do
			local ex=e1:Clone()
			ex:SetCode(code)
			tc:RegisterEffect(ex,true)
		end
	end)
	Duel.SendtoGrave(gg,REASON_RULE)
	if not Duel.Exile or Duel.Exile(eg,REASON_RULE)==0 then
		Duel.SendtoDeck(eg,nil,-2,REASON_RULE)
	end
	eg:ForEach(function(tc)
		tc:ResetEffect(0xfff0000,RESET_EVENT)
	end)
end
function get_save_location(c)
	if c:IsLocation(LOCATION_PZONE) then return LOCATION_PZONE
	else return c:GetLocation() end
end
function get_save_sequence(c)
	if c:IsOnField() then
		local seq=c:GetSequence()
		if c:IsLocation(LOCATION_PZONE) and seq==4 then seq=1 end
		return seq
	else return 0 end
end
function op(e,tp,eg,ep,ev,re,r,rp,c,sg,og)
	local p=e:GetHandler():GetOwner()
	local lc=e:GetLabel()
	local ctt={}
	for i=1,63 do
		table.insert(ctt,i)
	end
	if lc==256 then
		local g=e:GetLabelObject()
		local sg=Duel.GetMatchingGroup(sefilter,0,0x7f,0x7f,nil,g)
		if sg:GetCount()==0 then return end
		local tg=sg:Select(0,1,99,nil)
		ExileGroup(tg)
		return 
	end
	if lc==1024 then
		local sg=Duel.GetMatchingGroup(Card.IsType,0,LOCATION_MZONE,LOCATION_MZONE,nil,TYPE_XYZ)
		if sg:GetCount()==0 then return end
		local tg=sg:Select(0,1,63,nil)
		local cd=Duel.AnnounceCard(0)   
		local ct=Duel.AnnounceNumber(0,table.unpack(ctt))
		local tc=tg:GetFirst()
		while tc do
			local xg=Group.CreateGroup()
			for i=1,ct do
				local d=Duel.CreateToken(p,cd)
				d:CompleteProcedure()
				xg:AddCard(d)
			end
			Duel.Remove(xg,POS_FACEDOWN,0x20400)
			Duel.Overlay(tc,xg)
			tc=tg:GetNext()
		end
		return
	end
	local ftype=opcode_list[lc]
	local cd=0
	if ftype then
		cd=Duel.AnnounceCardFilter(0,table.unpack(ftype))
	else
		cd=Duel.AnnounceCard(0)
	end
	local ct=Duel.AnnounceNumber(0,table.unpack(ctt))
	for i=1,ct do
		local d=Duel.CreateToken(p,cd)
		if lc==1 then
			Duel.SendtoDeck(d,nil,0,0x20400)
		elseif lc==2 then
			Duel.SendtoHand(d,nil,0x20400)
		elseif lc==4 then
			local pos=nil
			if d:IsType(TYPE_LINK) then
				pos=POS_FACEUP_ATTACK
			else
				pos=Duel.SelectPosition(0,d,15)
			end
			Duel.MoveToField(d,0,p,lc,pos,true)
		elseif lc==8 then
			local pos=nil
			if d:IsType(TYPE_PENDULUM) then
				pos=POS_FACEUP_ATTACK
			else
				pos=Duel.SelectPosition(0,d,POS_ATTACK)
			end
			Duel.MoveToField(d,0,p,lc,pos,true)
		elseif lc==16 then
			Duel.SendtoGrave(d,0x20400)
		elseif lc==32 then
			local pos=Duel.SelectPosition(0,d,POS_ATTACK)
			Duel.Remove(d,pos,0x20400)
		elseif lc==64 then
			if d:IsType(TYPE_PENDULUM) then
				local pos=Duel.SelectPosition(0,d,POS_ATTACK)
				if pos==POS_FACEUP_ATTACK then
					Duel.SendtoExtraP(d,nil,0x20400)
				else
					Duel.SendtoDeck(d,nil,0,0x20400)
				end
			else
				Duel.SendtoDeck(d,nil,0,0x20400)
			end
		elseif lc==128 then
			local pos=nil
			if d:IsType(TYPE_LINK) then
				pos=POS_FACEUP_ATTACK
			else
				pos=Duel.SelectPosition(0,d,15)
			end
			if d:IsType(TYPE_PENDULUM) then
				Duel.SendtoExtraP(d,nil,0x20400)
			else
				Duel.SendtoDeck(d,nil,0,0x20400)
			end
			Duel.MoveToField(d,p,p,LOCATION_MZONE,pos,true)
		end
		d:CompleteProcedure()
	end
end
local original_card_group=Group.CreateGroup()
original_card_group:KeepAlive()
function reg(c,n,g)
	c:ResetEffect(c:GetOriginalCode(),RESET_CARD)
	local effect_list={
		EFFECT_CANNOT_TO_DECK,
		EFFECT_CANNOT_TO_HAND,
		EFFECT_CANNOT_REMOVE,
		EFFECT_CANNOT_SPECIAL_SUMMON,
		EFFECT_CANNOT_SUMMON,
		EFFECT_CANNOT_MSET,
		EFFECT_CANNOT_SSET,
		EFFECT_IMMUNE_EFFECT,
		EFFECT_CANNOT_BE_EFFECT_TARGET,
		EFFECT_CANNOT_CHANGE_CONTROL,
	}
	local effect_list_0={
		EFFECT_CHANGE_TYPE,
	}
	local e2=Effect.CreateEffect(c)
	e2:SetType(EFFECT_TYPE_FIELD)
	e2:SetCode(EFFECT_SPSUMMON_PROC_G)
	e2:SetProperty(EFFECT_FLAG_UNCOPYABLE+EFFECT_FLAG_CANNOT_DISABLE+EFFECT_FLAG_SET_AVAILABLE+EFFECT_FLAG_BOTH_SIDE)
	e2:SetRange(0xff)
	e2:SetLabel(n)
	e2:SetLabelObject(g)
	e2:SetOperation(op)
	c:RegisterEffect(e2)
	for i,v in pairs(effect_list) do
		local e6=Effect.CreateEffect(c)
		e6:SetType(EFFECT_TYPE_SINGLE)
		e6:SetCode(v)
		e6:SetProperty(0x40500+EFFECT_FLAG_IGNORE_IMMUNE)
		e6:SetValue(aux.TRUE)
		c:RegisterEffect(e6)
	end
	for i,v in pairs(effect_list_0) do
		local e6=Effect.CreateEffect(c)
		e6:SetType(EFFECT_TYPE_SINGLE)
		e6:SetCode(v)
		e6:SetProperty(0x40500+EFFECT_FLAG_IGNORE_IMMUNE)
		e6:SetValue(0)
		c:RegisterEffect(e6)
	end
end
for card_value,card_code in pairs(custom_list) do
	local a0=Debug.AddCard(card_code,0,0,card_location,0,POS_FACEUP_ATTACK)
	local a1=Debug.AddCard(card_code,1,1,card_location,0,POS_FACEUP_ATTACK)
	original_card_group:AddCard(a0)
	original_card_group:AddCard(a1)
	reg(a0,card_value,original_card_group)
	reg(a1,card_value,original_card_group)
end
--require("specials/special")
local ex=Effect.GlobalEffect()
ex:SetType(EFFECT_TYPE_FIELD)
ex:SetCode(EFFECT_TRAP_ACT_IN_SET_TURN)
ex:SetProperty(EFFECT_FLAG_SET_AVAILABLE)
ex:SetTargetRange(LOCATION_SZONE,LOCATION_SZONE)
Duel.RegisterEffect(ex,0)
local ex=Effect.GlobalEffect()
ex:SetType(EFFECT_TYPE_FIELD+EFFECT_TYPE_CONTINUOUS)
ex:SetCode(EVENT_ADJUST)
ex:SetProperty(EFFECT_FLAG_IGNORE_IMMUNE)
ex:SetLabelObject(original_card_group)
ex:SetOperation(function(e)
	local eg=e:GetLabelObject()
	local tc=eg:GetFirst()
	while tc do
		if not tc:IsLocation(LOCATION_GRAVE) then
			Duel.SendtoGrave(tc,REASON_RULE+REASON_RETURN)
		end
		tc=eg:GetNext()
	end
end)
Duel.RegisterEffect(ex,0)
local ex=Effect.GlobalEffect()
ex:SetType(EFFECT_TYPE_FIELD)
ex:SetCode(EFFECT_TRAP_ACT_IN_HAND)
ex:SetTargetRange(LOCATION_HAND,LOCATION_HAND)
Duel.RegisterEffect(ex,0)
local ex=Effect.GlobalEffect()
ex:SetType(EFFECT_TYPE_FIELD)
ex:SetCode(EFFECT_HAND_LIMIT)
ex:SetTargetRange(1,1)
ex:SetProperty(EFFECT_FLAG_PLAYER_TARGET)
ex:SetValue(100)
Duel.RegisterEffect(ex,0)

Debug.SetPlayerInfo(0,8000,0,0)
Debug.SetPlayerInfo(1,8000,0,0)
Debug.ReloadFieldEnd()
