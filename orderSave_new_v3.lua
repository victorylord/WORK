local dateExpect = ARGV[1];
local busId = ARGV[2];
local sportId = ARGV[3];
local userId = ARGV[4];
local matchId = ARGV[5];
local playId = ARGV[6];
local marketId = ARGV[7];
local matchType = ARGV[8];
local playType = ARGV[9];
local orderId = ARGV[10];
local betAmount = tonumber(ARGV[11]);
local isRecType = tonumber(ARGV[12]);
local optionId = ARGV[13];
local currentPaidMoney = ARGV[14];

local recData = ARGV[15];
local recLength = tonumber(ARGV[16]);

local userMatchPlayPaidMoneyByDB = tonumber(ARGV[17]);
local userMatchAllPaidAmountByDB = tonumber(ARGV[18]);
local userDayAllPaidAmountByDB = tonumber(ARGV[19]);
local singleMatchAllPaidAmountByDB = tonumber(ARGV[20]);

local odd = tonumber(ARGV[21]);

local resultTable = {};
resultTable["1"] = betAmount;
resultTable["2"] = betAmount / 2;
resultTable["3"] = betAmount * (odd - 1) * -1;
resultTable["4"] = betAmount / 2 * (odd - 1) * -1 ;
resultTable["5"] = 0;

local mytable = {};
mytable["0"] = {"0","0"};mytable["1"] = {"0","1"};mytable["2"] = {"0","2"};mytable["3"] = {"0","3"};mytable["4"] = {"0","4"};mytable["5"] = {"0","5"};mytable["6"] = {"0","6"};mytable["7"] = {"0","7"};mytable["8"] = {"0","8"};mytable["9"] = {"0","9"};mytable["A"] = {"1","0"};mytable["B"] = {"1","1"};mytable["C"] = {"1","2"};mytable["D"] = {"1","3"};mytable["E"] = {"1","4"};mytable["F"] = {"1","5"};mytable["G"] = {"1","6"};mytable["H"] = {"1","7"};mytable["I"] = {"1","8"};mytable["J"] = {"1","9"};mytable["K"] = {"2","0"};mytable["L"] = {"2","1"};mytable["M"] = {"2","2"};mytable["N"] = {"2","3"};mytable["O"] = {"2","4"};mytable["P"] = {"2","5"};mytable["Q"] = {"2","6"};mytable["R"] = {"2","7"};mytable["S"] = {"2","8"};mytable["T"] = {"2","9"};mytable["U"] = {"3","0"};mytable["V"] = {"3","1"};mytable["W"] = {"3","2"};mytable["X"] = {"3","3"};mytable["Y"] = {"3","4"};mytable["Z"] = {"3","5"};mytable["a"] = {"3","6"};mytable["b"] = {"3","7"};mytable["c"] = {"3","8"};mytable["d"] = {"3","9"};mytable["e"] = {"4","0"};mytable["f"] = {"4","1"};mytable["g"] = {"4","2"};mytable["h"] = {"4","3"};mytable["i"] = {"4","4"};mytable["j"] = {"4","5"};mytable["k"] = {"4","6"};mytable["l"] = {"4","7"};mytable["m"] = {"4","8"};mytable["n"] = {"4","9"};mytable["o"] = {"5","0"};mytable["p"] = {"5","1"};mytable["q"] = {"5","2"};mytable["r"] = {"5","3"};mytable["s"] = {"5","4"};mytable["t"] = {"5","5"};mytable["u"] = {"5","6"};mytable["v"] = {"5","7"};mytable["w"] = {"5","8"};mytable["x"] = {"5","9"};mytable["y"] = {"6","0"};mytable["z"] = {"6","1"};

local suffix = "_{" .. busId .. "_" .. matchId .. "}";
local prefix = "RCS:RISK:" .. dateExpect .. ":" .. busId .. ":" .. sportId .. ":";
local userMatchPlayMarketKey = prefix .. userId .. ":" .. matchId .. ":" .. playId .. ":" .. marketId .. ":" .. matchType .. ":" .. playType .. suffix;
local userMatchKey = prefix .. userId .. ":" .. matchId .. ":" .. matchType .. suffix;
local userKey = prefix .. userId;
local singleMatchInfoKey = prefix .. matchId ..":" .. matchType .. ":V2" .. suffix;
local singleMatchMarketKey = prefix .. matchId .. ":" .. playId .. ":" .. marketId .. suffix;

local errMsg = nil;
local diffVal = 0;

local optionMaxPaidMoney = redis.call("HINCRBYFLOAT",userMatchPlayMarketKey,optionId,currentPaidMoney);
local allOrderBetMoney = redis.call("HINCRBYFLOAT",userMatchPlayMarketKey,'allOrderMoney',betAmount);
local redisMaxOptionPaid = redis.call("hget",userMatchPlayMarketKey,'maxOptionPaid');

if not redisMaxOptionPaid then
	redisMaxOptionPaid = 0;
end;

if tonumber(redisMaxOptionPaid) < tonumber(optionMaxPaidMoney) then
	redisMaxOptionPaid = optionMaxPaidMoney;
	redis.call("hset",userMatchPlayMarketKey,'maxOptionPaid',redisMaxOptionPaid);
end;

local maxPlayPaid = tonumber(redisMaxOptionPaid) - tonumber(allOrderBetMoney);
local redisMaxPlayPaid = redis.call("hget",userMatchPlayMarketKey,'MAX_PLAY_PAID');
redis.call("hset",userMatchPlayMarketKey,'MAX_PLAY_PAID' , maxPlayPaid);

if not redisMaxPlayPaid then
	redisMaxPlayPaid = 0;
end;

diffVal = maxPlayPaid - tonumber(redisMaxPlayPaid);

local secondKey = playId .. '_' .. matchType .. '_' .. playType;
local userMatchPlayPaidMoney = redis.call("HINCRBYFLOAT",userMatchKey,secondKey,diffVal);

if tonumber(userMatchPlayPaidMoney) > userMatchPlayPaidMoneyByDB then
 	errMsg = "用户玩法赔付限额拒单，配置：" .. userMatchPlayPaidMoneyByDB .. ",计算后："  ..  userMatchPlayPaidMoney ;
	return {-2,errMsg};
end;

if isRecType == 0 then
	local min = nil;
	local allTable = {};

	local func_itor = {};
	local allKeysResult = redis.call("HGET",userMatchKey,"ALL_KEYS");
	if allKeysResult then
		func_itor = loadstring("return {" .. allKeysResult .. "}")();
	end;

	local index = 1;
	for i in string.gmatch(recData,"(.-),") do
		local tempI = mytable[i];
		if tempI == nil then
		    break;
		end;
		for j = 1 , #tempI do
			local currentResult = tempI[j];
			if currentResult == "9" then
				break;
			end;

			local money = resultTable[currentResult];
            if func_itor[index] then
                money = money + func_itor[index];
            end;
			
            func_itor[index] = money;
			if min == nil or min > money then
				min = money;
			end;

			index = index + 1;
		end;
	end;
	redis.call("hset",userMatchKey,"ALL_KEYS",table.concat(func_itor,","));
	
	local recMaxPaid = redis.call("hget",userMatchKey,"REC_MAX_PAID");
	if not recMaxPaid then
		recMaxPaid = 0;
	end;
	
	diffVal = tonumber(recMaxPaid) - min;
	
	redis.call("hset",userMatchKey,"REC_MAX_PAID" ,min);
end;

local userMatchAllPaidAmount = redis.call("HINCRBYFLOAT",userMatchKey,"USER_MATCH_ALL_PAID",diffVal);
if tonumber(userMatchAllPaidAmount) > userMatchAllPaidAmountByDB then
 	errMsg = "用户赛事限额拒单，配置：" .. userMatchAllPaidAmountByDB .. ",计算后："  ..  tonumber(userMatchAllPaidAmount) ;
	return {-3,errMsg};
end;

local diffValMatch = 0;
if isRecType == 0 then
	local min = nil;
	local allTable = {};

	local func_itor = {};
	local allKeysResult = redis.call("HGET",singleMatchInfoKey,"ALL_KEYS");
	if allKeysResult then
		func_itor = loadstring("return {" .. allKeysResult .. "}")();
	end;

	local index = 1;
	for i in string.gmatch(recData,"(.-),") do
		local tempI = mytable[i];
		if tempI == nil then
		    break;
		end;
		for j = 1 , #tempI do
			local currentResult = tempI[j];
			if currentResult == "9" then
				break;
			end;

			local money = resultTable[currentResult];
            if func_itor[index] then
                money = money + func_itor[index];
            end;
			
            func_itor[index] = money;
			if min == nil or min > money then
				min = money;
			end;

			index = index + 1;
		end;
	end;
	redis.call("hset",singleMatchInfoKey,"ALL_KEYS",table.concat(func_itor,","));
	
	local recMaxPaid = redis.call("hget",singleMatchInfoKey,"REC_MAX_PAID");
	if not recMaxPaid then
		recMaxPaid = 0;
	end;
	
	diffValMatch = tonumber(recMaxPaid) - min;
	
	redis.call("hset",singleMatchInfoKey,"REC_MAX_PAID" ,min);
else 
	local optionMaxPaidMoneyMatch = redis.call("HINCRBYFLOAT",singleMatchMarketKey,optionId,currentPaidMoney);
	local allOrderBetMoneyMatch = redis.call("HINCRBYFLOAT",singleMatchMarketKey,'allOrderMoney',betAmount);
	local redisMaxOptionPaidMatch = redis.call("hget",singleMatchMarketKey,'maxOptionPaid');
	
	if not redisMaxOptionPaidMatch then
		redisMaxOptionPaidMatch = 0;
	end;
	
	if tonumber(redisMaxOptionPaidMatch) < tonumber(optionMaxPaidMoneyMatch) then
		redisMaxOptionPaidMatch = optionMaxPaidMoneyMatch;
		redis.call("hset",singleMatchMarketKey,'maxOptionPaid',redisMaxOptionPaidMatch);
	end;
	
	local maxPlayPaidMatch = tonumber(redisMaxOptionPaidMatch) - tonumber(allOrderBetMoneyMatch);
	local redisMaxPlayPaidMatch = redis.call("hget",singleMatchMarketKey,'MAX_PLAY_PAID');
	redis.call("hset",singleMatchMarketKey,'MAX_PLAY_PAID' , maxPlayPaidMatch);
	
	if not redisMaxPlayPaidMatch then
		redisMaxPlayPaidMatch = 0;
	end;
	
	diffValMatch = maxPlayPaidMatch - tonumber(redisMaxPlayPaidMatch);
	redis.call("HSET",singleMatchInfoKey,playId,maxPlayPaidMatch);
end;

local singleMatchAllPaidAmount = redis.call("HINCRBYFLOAT",singleMatchInfoKey,'MAX_MATCH_PAID',diffValMatch);
if tonumber(singleMatchAllPaidAmount) > singleMatchAllPaidAmountByDB then
 	errMsg = "单场赛事限额拒单，配置：" .. singleMatchAllPaidAmountByDB .. ",计算后："  .. tonumber(singleMatchAllPaidAmount) ;
	return {-5,errMsg};
end;


--[[ redis 设置过期时间--]]
if tonumber(matchType)==1 then
	redis.call("expire", userMatchPlayMarketKey,   24*60*60);
	redis.call("expire", userMatchKey,   24*60*60);
	redis.call("expire", singleMatchInfoKey,   24*60*60);
	redis.call("expire", singleMatchMarketKey,   24*60*60);
end;
if  tonumber(matchType)==0 then
	redis.call("expire", userMatchPlayMarketKey,   90*24*60*60);
	redis.call("expire", userMatchKey,   90*24*60*60);
	redis.call("expire", singleMatchInfoKey,   90*24*60*60);
	redis.call("expire", singleMatchMarketKey,   90*24*60*60);
end;

return {1,'SUCCESS',userKey,diffVal};
