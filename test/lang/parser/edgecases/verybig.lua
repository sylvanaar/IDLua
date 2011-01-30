--[[

Some General Developer Notes

Some of the coding here is far from ideal ;p (I was learning Lua & XML while I wrote it, so buhu2u)
But at the time I write this, (v6.BETA), I don't have a gaming account to test with, and a complete re-write is out of the question :(

I have added some comments throughout the code.
And here are some general ramblings that might help someone who's puzzling over the code :

local_player.currentNote.unit & local_player.currentNote.general			:	These variables are global to the AddOn and refer to the note names for the currently Open 
						Contact & General notes respectively. Some functions process notes in the background while
						the user potentially has a note open, and the code will sometimes copy these values to temporary
						variables, use them in processing, and then reset them to the saved temporary values.
						I'm not sure this is necessary in some cases, but as I can't test any more, I'm sticking to the rule of
						thumb "If it ain't broke, don't fix it" Oh, for a OO approach from the very beginning ;P

Note Sizes (6006 characters)
						This is controlled by the constants : 
							NuNC.NUN_MAX_ADD_TXT = 5;
							NuNC.NUN_MAX_TXT_CHR = 1001;
						WoW originally limited the size of the text fields that could be saved in SavedVariables, so my solution
						was to split the note in to my own chunks of text and save them as sequential text fields.
						The splitting and concatenation of these chunks in to single notes is handled by the SetCText, GetCText,
						SetGText, and GetGText functions, (i.e. for Contact & General notes)
						To avoid Blizzard substituting invalid character sequences in to NotesUNeed notes when the game ends
						and Saved Variables are actually saved - NuN also substitutes certain special characters within notes.
						i.e. this stops Blizzard carrying out its own substitution of these characters which has been known to
						lead to crashes and loss of entire NuN databases.
						These character substitutions are carried out in the background and hidden from the end user, and are
						handled by the _SetSaveText, and _SetDisplayText functions.

User Definable Buttons			Users can define their own button values for the 10 buttons in the top left of the Contact note frame
						 There are 5 Headings buttons with default values, and 5 Details Buttons left blank by default
						 The first 2 Heading buttons are Guild, and Guild Rank by default, and if left un-changed, then NuN will try to auto-populate Guild information in to the first 2 Detail buttons
						 If a Heading is blanked, then the associated Detail button is disabled
						 They can have a different value for a specific Contact note saved in NuNData
						 OR They can have a new default for all Contacts saved in NuNSettings
						 OR NuN will use its own hard coded defaults

Note Saving Levels				i.e. Saving at Realm level or Account level
						Player Notes are always saved at Realm level, and the code doesn't have to worry about duplicates;
						Ofc, this means users can't see Player notes made on other Realms
						General Notes can be saved at Realm, or account level.
						The user is allowed to have multiple notes with the same name saved on different Realms.
						But can't have the same note saved at Account Level as well.
						NotesUNeed will give a warning when you are about to save a new note with the same name as an existing note

receiptPending			:	NuN allows Players to send each other Notes - See the various comments in the ChatMessage_EventHandler hook
						If someone sends you a note that has the same name as one of your existing notes, then you are presented
						with a duplicate record dialog box, and flagged as receiptPending, until you decide to either replace your note
						with the one being sent to you, or reject the one that has been sent to you.
						This flag stops you from doing things that could result in corrupted data, such as trying to alter the original
	 					note, or open a new one, and or displaying yet another note from a third party.....
						(Other players aren't informed that you are receiptPending - its just a local measure to try and prevent any data
						corruption until you make your mind up)

MapNotes integration			NotesUNeed allows creation of MapNotes at the Player's current location; And while standing in a single location, you can create multiple 
						MapNotes that will be merged on the same Map note - NotesUNeed creates an index of the notes merged this way,
						and shows a little sub-menu detailing the separate notes merged on that point when you mouse over the Map note - this sub-menu can itself be moused
						over (when the <control> key is pressed to show tooltips for each individual note, or clicked to open the note (or clicked to delete the note and remove
						it from the merged MapNotes)
						Best to experiment to understand - stand in one place with a few NPCs around.
						Have MapNotes (Fan's Update) installed
						Have auto-Map Noting of NPCs turned on.
						Without moving, create NotesUNeed notes for several NPCs around you
						You should see a few MapNotes creation messages in the chat frame.
						Open, the map and play with the MapNote thus created.
						
]]--


NotesUNeed = {
	locals = { player = {} },
	Strings = {},
	NuN_Statics = {},
	contact = {},
	general = {},
};

-- by this revision, we've surpassed the limit on the number of local variables allowed at a single scope (400), so we
-- need to move some of the local variables into tables, which is what the "locals" table will be for.
local locals = NotesUNeed.locals;

-- Saved Data
_G.NuNData = {}
_G.NuNSettings = {}

local NuNData = _G.NuNData;
local NuNSettings = _G.NuNSettings;

-- Duplicated deliberately here as well as localisation files as need to be able to access multiple versions from the code
locals.enHeadings = {
	"Guild : ",
	"Guild Rank : ",
	"Real Name : ",
	"e-mail : ",
	"WWW : "
};
locals.deHeadings = {
	"Gilde : ",
	"Gilden Rang  : ",
	"Echter Name : ",
	"e-mail : ",
	"WWW : "
};
locals.frHeadings = {
	"Guilde : ",
	"Grade de Guilde : ",
	"Vrai Nom : ",
	"e-mail : ",
	"WWW : "
};
locals.fromHeadings = {};

locals.Races = {};
locals.Ranks = {};
locals.Classes = {};
locals.foundNuN = {};
locals.continents = {};
locals.foundHNuN = {};
locals.foundANuN = {};
locals.foundNNuN = {};
locals.NuNQuestLog = {};

locals.NuN_Filtered = {};

locals.NuN_FriendUpdate = {};
locals.NuN_IgnoreUpdate = {};

locals.NuN_FriendIgnoreActivity = nil;

locals.NuN_LastOpen = {};

locals.NuN_TransStrings = {};
	locals.NuN_TransStrings[1] = {};
		locals.NuN_TransStrings[1][1] = "NotesUNeed NOTE TRANSMISSION FROM : ";
		locals.NuN_TransStrings[1][2] = "NotesUNeed NOTIZ\195\156BERTRAGUNG VON : ";
	locals.NuN_TransStrings[2] = {};
		locals.NuN_TransStrings[2][1] = "NotesUNeed NOTE BEGINS : ";
		locals.NuN_TransStrings[2][2] = "NotesUNeed NOTIZANFANG : ";
	locals.NuN_TransStrings[3] = {};
		locals.NuN_TransStrings[3][1] = "NotesUNeed NOTE ENDS   : ";
		locals.NuN_TransStrings[3][2] = "NotesUNeed NOTIZENDE   : ";


--[[
The following two tables group together all of the info about the note currently being viewed or opened.  I would like to move them to locals, but due to the fact
that this version (6.64) will also contain all of the changes necessary to support the 4.0.1 WoW patch (Cataclysm), I think I'll wait till that all settles down
before making such a risky change.
So, I'll leave the tables themselves at the global scope, and just stick all of the related data (which has always been declared at file scope, anyway) inside
these tables to reduce the number of local variables we have at file scope (as we're also hitting the upper limit on the number of local vars at a single scope).
--]]
local contact = NotesUNeed.contact;
local general = NotesUNeed.general;
-- below are more global variables associated with Contact / General Notes
--[[
local contact.class;
local contact.race;
local contact.sex;
local contact.prating;
local contact.prof1;
local contact.prof2;
local contact.arena;
local contact.hrank;
local contact.guild;
local contact.route;
local contact.text;
local contact.text_len;
local general.text;
local general.text_len;
local contact.type;
--]]

local local_player = locals.player;
local_player.currentNote = {
	unit = "",				-- global variable name of Player whose Contact Note is open, or was last opened
	general = "",			-- global variable name of General Note that is open, or was last opened
};
--[[
c_name & c_note are the MAIN global variables (i.e. global to the Script)
Not great coding form, but these will always reference the Open Note
The use of these variables is SO deeply ingrained, that it would take a re-write to make notes more OO
- Telic
--]]

--[[
:>	turned out to not be all that difficult, in the end.  moved the c_name and c_note vars into the contact
	table (which will remain globally accessible, tho I don't really think it needs to be anymore), and
	find-n-replace all references
- orgevo
--]]
local_player.currentNote.unit = "";
local_player.currentNote.general = "";

-- Holds an array of references for the various dropdown menus used by NotesUNeed
locals.dropdownFrames = {};

-- Misc
locals.bttnChanges = {};								-- record changes to User Definable buttons BEFORE the Note is Saved
locals.uBttns = getn(NUN_DFLTHEADINGS);
locals.detlOffset = locals.uBttns;
locals.pHead = "~Hdng";
locals.pDetl = "~Detl";
locals.discard = nil;
locals.prevName = nil;
locals.txtTxt = "txt";
locals.player_Name = nil;
locals.questHistory = {
	-- Name used for fetching Quest History, defaults to locals.player_Name (i.e. logged name)
	Tag = nil,
	-- Realm key used for fetching Quest History, defatulst to local_player.realmName (i.e. logged realm)
	Realm = nil,
	-- Cached values for the current quest index and note title
	Index = nil,
	Title = nil,
}
local_player.realmName = nil;
local_player.factionName = nil;
-- we could probably group the next 5 or so variables into their own subtable; they're all related to note categories or headers.
locals.headingNumber = nil;
locals.headingName = nil;
locals.headingDate = nil;
locals.originalText = nil;	-- generic cache variable used for storing original value of various controls
locals.searchType = nil;
locals.isTitle = nil;
locals.bttnNumb = nil;
locals.Notes_dbKey = "notes~";								-- database key
locals.itmIndex_dbKey = "ItmIndex~";						-- database key (related to saving of Notes with Item Links as Names)
locals.mrgIndex_dbKey = "merged~";							-- database key (related to merging of multiple MapNotes)
locals.prevNote = nil;
locals.lastBttn = nil;
locals.lastBttnIndex = nil;
locals.deletedE = nil;
locals.visibles = nil;
locals.lastVisible = nil;
locals.lastBttnDetl = nil;
locals.timeSinceLastUpdate = 0;
locals.popUpTimeSinceLastUpdate = 0;
locals.NuNRaceDropDown = nil;
locals.NuNClassDropDown = nil;
locals.NuNHRankDropDown = nil;
locals.NuN_rType = nil;
locals.ttName = nil;
locals.gtName = nil;
locals.NuN_TT_Y_Offset = 0;
locals.NuN_GNote_OriTitle = nil;
--[[
don't seem to be used anywhere...
local NuN_Fingers;
local NuN_Trinkets;
local NuN_Hand;
--]]
locals.sendTo = nil;
locals.msgSeq = 0;
locals.rMsgSeq = 0;
locals.popUpHide = nil;
locals.qTriggs = 0;
locals.noTipAnchor = nil;
--local NuN_QuestAccepted = nil;
locals.NuN_Receiving = {};
local defaultReceiptDeadline = 3;
local receiptDeadline = defaultReceiptDeadline;
local NuN_uCount = 999;
local NuN_tCount = 999;
local rBuff = "~receiptBuffer";
local receiptPending = nil;
local NuN_msgKey = nil;
local NuN_transmissionTimer = 0;
local NuN_AttemptedFriendIgnores = 0;
local delayedItemTooltip = nil;
local NuN_QLF = nil;
local lastTextKey = "";
local filterText = "";
local suppressDateUpdate = nil;
local NuN_horde = nil;
local friendsPendingUpdate = nil;
local ignoresPendingUpdate = nil;
local NuN_ImportModule = nil;
local NuN_ImportData = nil;
local NPCInfo_Proceed = nil;
local NuN_WhoReturnStruct = {};		-- 5.60
local bttnTxtObj;					-- 5.60
local ddProfSearch;					-- 5.60
local ddClassSearch;				-- 5.60
local ddQHSearch;					-- 5.60
local gRank;
local gRankIndex;
local gNote;
local gOfficerNote;
locals.lastDD = nil;	-- allows one DropDown box change to be undone on the Contact Note Edit frame
locals.noteNameLabel = nil;
locals.nameLastAttemptedFriendUpdate = nil;
locals.nameLastAttemptedIgnoreUpdate = "";


locals.NuNDebug = false;
locals.debugging_msg_hooks = true;
locals.processAddMessage = true;


--[[====================================================================
	====================================================================
	STRINGS
	====================================================================
	====================================================================]]
local NuN_Strings = NotesUNeed.Strings;
NuN_Strings.NUN_NOTESUNEED_INFO = NUN_NOTESUNEED_INFO;
NuN_Strings.NUN_PARTY = NUN_PARTY;
NuN_Strings.NUN_GUILD = NUN_GUILD;

--[[====================================================================
	====================================================================
	UPVALUES
	====================================================================
	====================================================================]]
--[[
--local NuN_NoteDB = NuNData;
--local NuN_SettingDB = NuNSettings;

--	tried to use a local variable on these saved variables, but I must have overlooked a back-assignment somewhere because simple find-n-replace
	results in no data being loaded by the frame, even though it exists in the NuNData variable.
	This function can be used to help track down those issues...
function DebugNUNData()
	NuN_Message("Dumping summary of local NuN_NoteDB table contents");
	NuN_Message("    Total entries:" .. getn(NuN_NoteDB));
	local idx = 1;
	for key, val in pairs(NuN_NoteDB) do
		NuN_Message(tostring(idx) .. ") " .. tostring(key));
		idx = idx + 1;
		
		local note_idx = 1;
		for note_name, note_text in pairs(val) do
			NuN_Message("   " .. tostring(note_idx) .. ") " .. tostring(note_name));
			note_idx = note_idx + 1;
		end
		
	end
end
--]]

--[[====================================================================
	Library Functions
	====================================================================]]
local tsort, getn, tonumber, tostring = table.sort, getn, tonumber, tostring
local strlen, strfind, strsub, strrep, strgsub, strlower, strformat, strbyte, strchar, strupper, strformat, strgmatch = string.len, string.find, string.sub, string.rep, string.gsub, string.lower, string.format, string.byte, string.char, string.upper, string.format, string.gmatch
local pairs, ipairs, next, type = pairs, ipairs, next, type
--local bit_ls, bit_rs, bit_or, bit_and, bit_not = bit.lshift, bit.rshift, bit.bor, bit.band, bit.bnot

--[[====================================================================
	Blizzard API functions
	====================================================================]]
local GetTime, GetCursorPosition = GetTime, GetCursorPosition;
local GetFriendInfo = GetFriendInfo;
local GetMouseFocus = GetMouseFocus;
local GetNumIgnores = GetNumIgnores;
local GetIgnoreName = GetIgnoreName;
local MouseIsOver, IsAltKeyDown = MouseIsOver, IsAltKeyDown;
local UnitAffectingCombat = UnitAffectingCombat;
local UnitExists = UnitExists;
local UnitName, UnitIsPlayer = UnitName, UnitIsPlayer;

local UIDropDownMenu_SetWidth = UIDropDownMenu_SetWidth;
local UIDropDownMenu_SetSelectedID = UIDropDownMenu_SetSelectedID;
local UIDropDownMenu_SetText = UIDropDownMenu_SetText;

local GuildRoster = GuildRoster;

--[[====================================================================
	Consts
	====================================================================]]
local UNKNOWN = UNKNOWN;
local UNKNOWNOBJECT = UNKNOWNOBJECT;

--[[====================================================================
	UI Objects
	====================================================================]]
local UIParent = UIParent;
local GameTooltip, GameTooltipTextLeft1 = GameTooltip, GameTooltipTextLeft1;
local ShoppingTooltip1, ShoppingTooltip2 = ShoppingTooltip1, ShoppingTooltip2;
local FriendsFrame, FriendsListFrame, IgnoreListFrame = FriendsFrame, FriendsListFrame, IgnoreListFrame;
local WhoFrame, WorldMapFrame, QuestLogFrame, UnitPopupMenus = WhoFrame, WorldMapFrame, QuestLogFrame, UnitPopupMenus;
local TargetFrame, RaidFrame, GuildFrame = TargetFrame, RaidFrame, GuildFrame;
local DEFAULT_CHAT_FRAME = DEFAULT_CHAT_FRAME;

--[[====================================================================
	Local functions which we'll define later
	====================================================================]]
local NuN_AutoNote;
local NuN_Update_Ignored;
local NuN_UpdateNoteButton;
local NuNNew_UnitPopup_OnClick;
local NuNNew_UnitPopup_ShowMenu;
local NuN_GetTipAnchor;

--[[====================================================================
	References to external objects we've created
	====================================================================]]
local NuNC = NuNC;
local NuN_Statics = NotesUNeed.NuN_Statics;


--[[====================================================================
	Objects defined in NotesUNeed.xml file AFTER this file is included
	(so they cannot be assigned at the file scope)
	====================================================================]]
local NuNMicroFrame;
local NuNPopup;
local NuNMicroBorder;
local NuNSexDropDown;
local NuNPRatingDropDown;
local NuNProf1DropDown;
local NuNProf2DropDown;
local NuNArenaRDropDown;
local NuNOptionsSearchDropDown;
local NuNChatDropDown;
local NuNChannelDropDown;
local NuNSearchClassDropDown;
local NuNSearchProfDropDown;
local NuNGTypeDropDown;
local NuNSearchQHDropDown;
local NuNARaceDropDown, NuNHRaceDropDown;
local NuNAClassDropDown, NuNHClassDropDown;
local NuNAHRankDropDown, NuNHHRankDropDown;
-- Tooltips
local NuN_Tooltip, NuN_PinnedTooltip, NuN_MapTooltip;

function NuN_InitializeUpvalues()
	NuNMicroFrame = _G.NuNMicroFrame;
	NuNPopup = _G.NuNPopup;
	NuNMicroBorder = _G.NuNMicroBorder;
	NuNSexDropDown = _G.NuNSexDropDown;
	NuNPRatingDropDown = _G.NuNPRatingDropDown;
	NuNProf1DropDown = _G.NuNProf1DropDown;
	NuNProf2DropDown = _G.NuNProf2DropDown;
	NuNArenaRDropDown = _G.NuNArenaRDropDown;
	NuNOptionsSearchDropDown = _G.NuNOptionsSearchDropDown;
	NuNChatDropDown = _G.NuNChatDropDown;
	NuNChannelDropDown = _G.NuNChannelDropDown;
	NuNSearchClassDropDown = _G.NuNSearchClassDropDown;
	NuNSearchProfDropDown = _G.NuNSearchProfDropDown;
	NuNGTypeDropDown = _G.NuNGTypeDropDown;
	NuNSearchQHDropDown = _G.NuNSearchQHDropDown;

	NuNARaceDropDown = _G.NuNARaceDropDown;
	NuNAClassDropDown = _G.NuNAClassDropDown;
	NuNAHRankDropDown = _G.NuNAHRankDropDown;
	
	NuNHRaceDropDown = _G.NuNHRaceDropDown;
	NuNHClassDropDown = _G.NuNHClassDropDown;
	NuNHHRankDropDown = _G.NuNHHRankDropDown;
	
	NuN_Tooltip = _G.NuN_Tooltip;
	NuN_PinnedTooltip = _G.NuN_PinnedTooltip;
	NuN_MapTooltip = _G.NuN_MapTooltip;
end

-- NuN States
local NuN_State = {};
	NuN_State.inBG = false;
	NuN_State.NuN_FirstTime = true;
	NuN_State.NuN_PinUpHeader = false;
	NuN_State.pinnedTTMoved = true;
	NuN_State.NuN_Fade = false;
	NuN_State.NuN_MouseOver = false;
	NuN_State.NuN_QuestsUpdating = false;
	NuN_State.oneDone = false;
	NuN_State.NuN_AtStartup = true;
	NuN_State.NuN_syncGuildMemberNotes = false;
	NuN_State.togglePinUp = false;
	NuN_State.ddHiked = true;
	
-- this flag allows us to prevent recursively entering the ShowNote() function unintentionally.
-- it's possible we could incorporate this into the NuN_State enum (and would make more sense)
-- but too much potential for side-effects from code expecting particular states during the note-opening process.
local m_ShowingNoteMutex = false;

local busySending = {};
	busySending.user = "";
	busySending.active = nil;
	busySending.counter = 0;

local sendToChannel = {};
	sendToChannel.id = 0;
	sendToChannel.name = "";

-- 5.60 abridged Database entries
locals.NuNDataPlayers = {};
local NuNDataANotes = {};
local NuNDataRNotes = {};
local NuNQuestHistory = {};

-- 5.60 Array for holding Alts
local AltArray = {};

local maxRatings = 26;

local NuNTalents = {};

----------------------------------------------------------------------------------------------------------------------------
-- Local Function Hooks -- NOT 'Securing' the following for the time being;
-- (Lets see if they 'really' produce any errors - seem OK for last couple of weeks)
NotesUNeed.NuNHooks = {};
local NuNHooks = NotesUNeed.NuNHooks;

----------------------------------------------------------------------------------------------------------------------------

-- Delayed Transmission Variables	-- Leaving Reception untouched apart from allowing more time for message to be fully sent; Only delaying by a fixed amount between Transmissions
local NuN_DTrans = {};
	NuN_DTrans.Status = "Inactive";
	NuN_DTrans.Prefix = "";
	NuN_DTrans.pArray = {};
	NuN_DTrans.tDelay = 0.9;	-- 3 per second default -- Multiply by number of messages and add default Wait to calculate the amount of time to allow for the final message to arrive after the first was sent
	NuN_DTrans.tTrack = 0;
	NuN_DTrans.aIndex = 0;
	NuN_DTrans.Params = {};

-- Keep Global cos' I likes it ;)
-- now iz local, mang!
local function NuN_Message(msg, r, g, b)
	if ( not msg ) then
		msg = " {NIL Value Passed}";
	elseif ( type(msg) == "table" ) then
		msg = " {TABLE Value Passed}";
	elseif ( type(msg) == "boolean" ) then
		msg = tostring(msg);
	end
	msg = "NotesUNeed: "..msg;
	if ( not r ) then
		r, g, b = 0.64, 0.21, 0.93;						--   |c00A335ED
	end
	if DEFAULT_CHAT_FRAME then
		DEFAULT_CHAT_FRAME.printingDebugMessage = true;
		DEFAULT_CHAT_FRAME:AddMessage(msg, r, g, b);
		DEFAULT_CHAT_FRAME.printingDebugMessage = nil;
	end
end

local function nun_msgf(str, ...)
--[===[@debug@
	NuN_Message(strformat(str, ...));
--@end-debug@]===]
end

-- simple wrapper for hiding the main notes frame, in cases where injecting some debugging is needed.
function HideNUNFrame()
	NuNFrame:Hide();
end




--[[

Uncomment this code to print out messages to the log anytime code within this file (which occurs after this
function) attempts to modify or otherwise access global variables.  Helpful when tracking down those last
few global references that need to be converted to local.
--]-]
local _G = _G;	-- save a reference to the global environment
setfenv(1, setmetatable( {}, {
	__index = function(t, k)
		local v = _G[k]
		_G.print("Accessing global variable " .. k .. "; current value: " .. _G.tostring(v))
		return v;
	end,
	__newindex = function(t, k, v)
		local oldValue = _G[k];
		print("Setting variable in global environment " .. k .. " to " .. _G.tostring(v) .. "; old value: " .. _G.tostring(oldvalue));
		_G[k] = v;
	end
}))
--]]

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------
-- Warning & Confirmation Dialog Boxes--
------------------------------------------------------


-- NotesUNeed Note Character Limit Exceeded
StaticPopupDialogs["NUN_NOTELIMIT_EXCEEDED"] = {
	text = TEXT(NUN_TEXTLIM1..NUN_TEXTLIM2),
	button1 = TEXT(OKAY),
	OnShow = function(self)
		_G[self:GetName().."Text"]:SetText( NUN_TEXTLIM1 .. NuNC.NUN_MAX_TXT_LIM .. NUN_TEXTLIM2 );
	end,
	showAlert = 1,
	timeout = 0,
	hideOnEscape = 1,
};

local oldRating, ratingChosen, chosenRating, curRatingTxt = false;
-- NotesUNeed Choose Note Importing type for Social Notes
StaticPopupDialogs["NUN_CHANGE_RATING_ONE"] = {
	text = TEXT(NUN_CHOOSE_RATING),
	button1 = ACCEPT,
	button2 = CANCEL,
	hasEditBox = 1,
	maxLetters = 28,
	OnShow = function()
		StaticPopup1EditBox:SetText( NuNSettings.ratings[chosenRating] );
		StaticPopup1EditBox:HighlightText();
		StaticPopup1EditBox:SetFocus(true);
		curRatingTxt = UIDropDownMenu_GetText( NuNPRatingDropDown );

		if ( BlackList ) then
			NuN_BLCheckBox:SetParent(StaticPopup1);
			NuN_BLCheckBox:SetFrameLevel( StaticPopup1:GetFrameLevel() + 2 );
			NuN_BLCheckBox:ClearAllPoints();
			NuN_BLCheckBox:SetPoint("TOPLEFT", StaticPopup1, "TOPLEFT", 11, -20);
			NuN_BLCheckBox:SetChecked( false );
			NuN_BLCheckBox_Ignore:SetChecked( false );
			NuN_BLCheckBox_Warn:SetChecked( false );
			NuN_BLCheckBox:Show();
			local r = NuNSettings.ratingsBL[chosenRating];
			if ( r > 0 ) then
				NuN_BLCheckBox:SetChecked( true );
				if ( ( r == 2 ) or ( r == 4 ) ) then
					NuN_BLCheckBox_Ignore:SetChecked( true );
				end
				if ( r > 2 ) then
					NuN_BLCheckBox_Warn:SetChecked( true );
				end
				NuN_BLCheckBox_Ignore:Show();
				NuN_BLCheckBox_Warn:Show();

			else
				NuN_BLCheckBox_Ignore:Hide();
				NuN_BLCheckBox_Warn:Hide();
			end
		end
	end,
	OnAccept = function(self)
		local text = StaticPopup1EditBox:GetText();
		if ( text ~= "" ) then
			if ( ( curRatingTxt ) and ( curRatingTxt ~= "" ) and ( oldRating ) ) then
				UIDropDownMenu_SetText(NuNPRatingDropDown, text);
			end
			NuNSettings.ratings[chosenRating] = text;
			if ( NuNSettings[local_player.realmName].rightClickMenu == true ) then
				NuN_SetupRatings();
			end
			local r = 0;
			if ( NuN_BLCheckBox:GetChecked() ) then
				r = 1;
			end
			if ( NuN_BLCheckBox_Ignore:GetChecked() ) then
				r = r + 1;
			end
			if ( NuN_BLCheckBox_Warn:GetChecked() ) then
				r = r + 2;
			end
			NuNSettings.ratingsBL[chosenRating] = r;
			ratingChosen = true;
		end
	end,
	EditBoxOnEnterPressed = function(self)
		local text = StaticPopup1EditBox:GetText();
		if ( text ~= "" ) then
			if ( ( curRatingTxt ) and ( curRatingTxt ~= "" ) and ( oldRating ) ) then
				UIDropDownMenu_SetText(NuNPRatingDropDown, text);
			end
			NuNSettings.ratings[chosenRating] = text;
			if ( NuNSettings[local_player.realmName].rightClickMenu == true ) then
				NuN_SetupRatings();
			end
			if ( NuN_BLCheckBox:GetChecked() ) then
				r = 1;
			end
			if ( NuN_BLCheckBox_Ignore:GetChecked() ) then
				r = r + 1;
			end
			if ( NuN_BLCheckBox_Warn:GetChecked() ) then
				r = r + 2;
			end
			NuNSettings.ratingsBL[chosenRating] = r;
			ratingChosen = true;
		end
		self:GetParent():Hide();
	end,
	EditBoxOnEscapePressed = function(self)
		self:GetParent():Hide();
	end,
	OnHide = function()
		NuN_BLCheckBox:Hide();
		NuN_BLCheckBox_Ignore:Hide();
		NuN_BLCheckBox_Warn:Hide();
		StaticPopup1EditBox:SetText("");
		if ( ratingChosen ) then
			StaticPopup_Show("NUN_CHANGE_RATING_TWO");
		end
	end,
	timeout = 0,
	hideOnEscape = 1,
};

-- NotesUNeed Choose Player rating tooltips
StaticPopupDialogs["NUN_CHANGE_RATING_TWO"] = {
	text = TEXT(NUN_CHOOSE_RATING_TEXT),
	button1 = ACCEPT,
	button2 = CANCEL,
	hasEditBox = 1,
	maxLetters = 512,
	OnShow = function()
		StaticPopup1EditBox:SetText( NuNSettings.ratingsT[chosenRating] );
		StaticPopup1EditBox:HighlightText();
		StaticPopup1EditBox:SetFocus(true);
	end,
	OnAccept = function(self)
		local text = StaticPopup1EditBox:GetText();
		if ( text ~= NuNSettings.ratingsT[chosenRating] ) then
			NuNSettings.ratingsT[chosenRating] = text;
		end
	end,
	EditBoxOnEnterPressed = function(self)
		local text = StaticPopup1EditBox:GetText();
		if ( text ~= NuNSettings.ratings[chosenRating] ) then
			NuNSettings.ratingsT[chosenRating] = text;
		end
		self:GetParent():Hide();
	end,
	EditBoxOnEscapePressed = function(self)
		self:GetParent():Hide();
	end,
	OnHide = function()
		StaticPopup1EditBox:SetText("");
	end,
	timeout = 0,
	hideOnEscape = 1,
};

-- Import NotesUNeed Notes
StaticPopupDialogs["NUN_IMPORT_NOTES"] = {
	text = TEXT(NUN_OPT_IMPORT),
	button1 = TEXT(NUN_OPT_IMPORT),
	button2 = TEXT(CANCEL),
	showAlert = 1,
	timeout = 0,
	OnShow = function(self)
		local recordCount = 0;
		for record, records in pairs(NuN_ImportData) do
			recordCount = recordCount + 1;
		end
		recordCount = "\n{"..recordCount.."}";
		_G[self:GetName().."Text"]:SetText( NuN_ImportModule..recordCount );
	end,
	OnAccept = function()
		local numImported = 0;
		local n = NuNGet_CommandID(NUN_SEARCHFOR, "Notes");
		local notesTxt = NUN_SEARCHFOR[n].Display;
		for note, data in pairs(NuN_ImportData) do
			NuNDataANotes[note] = {};
			NuNDataANotes[note] = data;
			numImported = numImported + 1;
		end
		NuN_Message( NUN_FINISHED_PROCESSING.." - "..NUN_OPT_IMPORT.." : "..numImported.." "..notesTxt );
	end,
	hideOnEscape = 1,
};

-- Mass Delete
StaticPopupDialogs["NUN_MASS_DELETE_CONFIRM"] = {
	text = TEXT(NUN_MASS_DELETE),
	button1 = TEXT(NUN_MASS_DELETE),
	button2 = TEXT(CANCEL),
	showAlert = 1,
	timeout = 0,
	OnAccept = function()
		local numDeleted = 0;
		local n = NuNGet_CommandID(NUN_SEARCHFOR, "Notes");
		local notesTxt = NUN_SEARCHFOR[n].Display;
		local toDelete = locals.foundNuN;
		local deletes = getn(toDelete);			-- #toDelete ?

		if ( NuNFrame:IsVisible() ) then
			HideNUNFrame();
		end
		if ( NuNGNoteFrame:IsVisible() ) then
			NuNGNoteFrame:Hide();
		end

		for i=1, deletes, 1 do
			local noteType = strsub( toDelete[i], 1, 1 );
			local noteName = strsub( toDelete[i], 2 );

			if ( ( noteType == NuNC.NUN_HORD_C ) or ( noteType == NuNC.NUN_ALLI_C ) ) then
				if ( locals.NuNDataPlayers[noteName] ) then
					if ( ( NuNSettings[local_player.realmName].autoA ) and ( ( locals.NuNDataPlayers[noteName].friendLst )
													       or ( locals.NuNDataPlayers[noteName].ignoreLst ) ) ) then
						-- don't delete friends / ignores if auto maintaining lists
					elseif ( ( NuNSettings[local_player.realmName].autoN ) and ( locals.NuNDataPlayers[noteName].type == NuNC.NUN_SELF_C ) ) then
						-- don't delete self note if automated
					else
						locals.NuNDataPlayers[noteName] = nil;
						numDeleted = numDeleted + 1;
						for n = 1, locals.uBttns, 1 do
							locals.headingName = noteName.. locals.pHead .. n;
							locals.headingDate = noteName.. locals.pDetl .. n;
							if ( locals.NuNDataPlayers[locals.headingName] ) then
								locals.NuNDataPlayers[locals.headingName] = nil;
							end
							if ( locals.NuNDataPlayers[locals.headingDate] ) then
								locals.NuNDataPlayers[locals.headingDate] = nil;
							end
						end
					end
				end

			else
				if ( NuNDataANotes[noteName] ) then
					NuNDataANotes[noteName] = nil;
					numDeleted = numDeleted + 1;

				elseif ( NuNDataRNotes[noteName] ) then
					NuNDataRNotes[noteName] = nil;
					numDeleted = numDeleted + 1;
				end
			end
		end

		if ( FriendsListFrame:IsVisible() ) then
			NuNNew_FriendsList_Update();
		elseif ( IgnoreListFrame:IsVisible() ) then
			NuNNew_IgnoreList_Update();
		elseif ( WhoFrame:IsVisible() ) then
			NuNNew_WhoList_Update();
		end
		if ( GuildFrame and GuildFrame:IsVisible() ) then
			NuNNew_GuildStatus_Update();
		end
		NuN_Message( NUN_FINISHED_PROCESSING.." - "..NUN_MASS_DELETE.." : "..numDeleted.." "..notesTxt );
		if ( NuNSearchFrame:IsVisible() ) then
			NuNSearchFrame:Hide();
			NuN_DisplayAll();			-- force a full data refresh
			NuNSearchFrame:Hide();
		end
	end,
	hideOnEscape = 1,
};

-- NotesUNeed Confirm Delete of Quest History
StaticPopupDialogs["NUN_DELETE_QUESTHISTORY"] = {
	text = TEXT(DELETE),
	button1 = TEXT(NUN_DELETE),
	button2 = TEXT(CANCEL),
	OnAccept = function()
		if ( ( not NuNGNoteFrame.fromQuest ) and ( NuNGNoteFrame:IsVisible() ) ) then
			NuNGNoteFrame.fromQuest = local_player.currentNote.general;
		end
		NuNQuestHistory[NuNGNoteFrame.fromQuest] = nil;
		locals.deletedE = true;
		NuN_FetchQuestHistory();
	end,
	showAlert = 1,
	timeout = 0,
	hideOnEscape = 1,
};









-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------
-- Local Functions --
--------------------------

local NuNF = {};

-- return true if Ignored
function NuNF.NuN_IsPlayerIgnored(player)
	if ( ( locals.NuNDataPlayers ) and ( locals.NuNDataPlayers[player] ) and ( locals.NuNDataPlayers[player].ignoreLst ) ) then
		return true;

	else
		for i = 1, GetNumIgnores(), 1 do
			local lName = GetIgnoreName(i);
			if ( player == lName ) then
				return true;
			end
		end
	end

	return nil;
end

-- locals.bttnChanges records the changes made to the 10 User definable Butons in the top left of the Contact Note Frame
-- i.e. it is an array that records changes BEFORE the actual Note is Saved; When the Note is Saved, then this array
--	can be reset
function NuNF.ClearButtonChanges()
	for i = 1, (locals.uBttns * 2), 1 do
		locals.bttnChanges[i] = "";
	end
end

-- Little helper function; NuN saves the indexes of Drop Down boxes, rather than the Text
function NuNF.NuNGet_TableID(_tab, txt)
	for i = 1, getn(_tab), 1 do					-- #_tab
		if ( _tab[i] == txt ) then return i; end
	end
	return nil;
end

-- Users can define their own button values for the 10 buttons in the top left of the Contact note frame
-- There are 5 Headings buttons with default values, and 5 Details Buttons left blank by default
-- The first 2 Heading buttons are Guild, and Guild Rank by default, and if left un-changed, then NuN will try to auto-populate Guild information in to the first 2 Detail buttons
-- If a Heading is blanked, then the associated Detail button is disabled
-- Headings can have a different value for a specific Contact note saved in NuNData
-- OR They can have a new default for all Contacts saved in NuNSettings
-- OR NuN will use its own hard coded defaults from the array NUN_DFLTHEADINGS
function NuNF.UserButtons_Initialise()
	local bttnHeadingText, bttnDetailText, bttnDetail
	for n = 1, locals.uBttns, 1 do
		bttnHeadingText = _G["NuNTitleButton"..n.."ButtonTextHeading"];
		bttnDetailText = _G["NuNInforButton"..n.."ButtonTextDetail"];
		bttnDetail = _G["NuNInforButton"..n];
		locals.headingNumber = locals.pHead..n;
		locals.headingName = local_player.currentNote.unit.. locals.headingNumber;
		locals.headingDate = local_player.currentNote.unit.. locals.pDetl .. n;
		if ( locals.NuNDataPlayers[locals.headingName] ) then
			bttnHeadingText:SetText(locals.NuNDataPlayers[locals.headingName].txt);
		elseif (NuNSettings[local_player.realmName][locals.headingNumber]) then
			bttnHeadingText:SetText(NuNSettings[local_player.realmName][locals.headingNumber].txt);
		else
			bttnHeadingText:SetText(NUN_DFLTHEADINGS[n]);
		end
		if ( ( bttnHeadingText:GetText() == nil ) or ( bttnHeadingText:GetText() == "" ) ) then  -- 5.60 Shouldn't this check for  "" rather than nil (or at least both "" or nil)
			bttnDetailText:SetText("");
			bttnDetail:Disable();
		else
			bttnDetail:Enable();
			if ( locals.NuNDataPlayers[locals.headingDate] ) then
				bttnDetailText:SetText(locals.NuNDataPlayers[locals.headingDate].txt);
			else
				bttnDetailText:SetText("");
			end
		end

		if ( n == 1 ) and ( contact.guild ~= nil ) then
			if ( bttnHeadingText:GetText() == NUN_DFLTHEADINGS[n] ) and ( (bttnDetailText:GetText() == "") or (bttnDetailText:GetText() == nil) ) then
				bttnDetailText:SetText(contact.guild);
				locals.bttnChanges[n+locals.detlOffset] = contact.guild;
			end
		end
		if ( n == 2 ) and ( gRank ~= nil ) then
			if ( bttnHeadingText:GetText() == NUN_DFLTHEADINGS[n] ) and ( (bttnDetailText:GetText() == "") or (bttnDetailText:GetText() == nil) ) then
				if ( gRankIndex == 0 ) then
					GuildRank = ("GM : "..gRank);
				else
					GuildRank = (gRankIndex.." : "..gRank);
				end
				bttnDetailText:SetText(GuildRank);
				locals.bttnChanges[n+locals.detlOffset] = GuildRank;
			end
		end
	end
end


-- Initialise the Drop Down Boxes on the Contact Note Frame based on saved data
function NuNF.DropDowns_Initialise()
	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].race ~= nil ) then
		UIDropDownMenu_SetSelectedID(locals.NuNRaceDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].race);
		UIDropDownMenu_SetText(locals.NuNRaceDropDown, locals.Races[ (locals.NuNDataPlayers[local_player.currentNote.unit].race) ]);
	elseif ( contact.race ~= nil ) then
		locals.dropdownFrames.ddRace = NuNF.NuNGet_TableID(locals.Races, contact.race);
		UIDropDownMenu_SetSelectedID(locals.NuNRaceDropDown, locals.dropdownFrames.ddRace);
		UIDropDownMenu_SetText(locals.NuNRaceDropDown, contact.race);
	else
		UIDropDownMenu_ClearAll(locals.NuNRaceDropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].cls ~= nil ) then
		UIDropDownMenu_SetSelectedID(locals.NuNClassDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].cls);
		UIDropDownMenu_SetText(locals.NuNClassDropDown, locals.Classes[ (locals.NuNDataPlayers[local_player.currentNote.unit].cls) ]);
	elseif ( contact.class ~= nil ) then
		locals.dropdownFrames.ddClass = NuNF.NuNGet_TableID(locals.Classes, contact.class);
		UIDropDownMenu_SetSelectedID(locals.NuNClassDropDown, locals.dropdownFrames.ddClass);
		UIDropDownMenu_SetText(locals.NuNClassDropDown, contact.class);
	else
		UIDropDownMenu_ClearAll(locals.NuNClassDropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].sex ~= nil ) then
		UIDropDownMenu_SetSelectedID(NuNSexDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].sex);
		UIDropDownMenu_SetText(NuNSexDropDown, NUN_SEXES[ (locals.NuNDataPlayers[local_player.currentNote.unit].sex) ]);
	elseif ( contact.sex ~= nil ) then
		locals.dropdownFrames.ddSex = contact.sex;
		contact.sex = NUN_SEXES[locals.dropdownFrames.ddSex];
		UIDropDownMenu_SetSelectedID(NuNSexDropDown, locals.dropdownFrames.ddSex);
		UIDropDownMenu_SetText(NuNSexDropDown, contact.sex);
	else
		UIDropDownMenu_ClearAll(NuNSexDropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prating ~= nil ) then
		UIDropDownMenu_SetSelectedID(NuNPRatingDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].prating);
		UIDropDownMenu_SetText(NuNPRatingDropDown, NuNSettings.ratings[ locals.NuNDataPlayers[local_player.currentNote.unit].prating ]);
	elseif ( contact.prating ~= nil ) then
		locals.dropdownFrames.ddPRating = contact.prating;
		contact.prating = NuNSettings.ratings[locals.dropdownFrames.ddPRating];
		UIDropDownMenu_SetSelectedID(NuNPRatingDropDown, locals.dropdownFrames.ddPRating);
		UIDropDownMenu_SetText(NuNPRatingDropDown, contact.prating);
	else
		UIDropDownMenu_ClearAll(NuNPRatingDropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prof1 ~= nil ) then
		UIDropDownMenu_SetSelectedID(NuNProf1DropDown, locals.NuNDataPlayers[local_player.currentNote.unit].prof1);
		UIDropDownMenu_SetText(NuNProf1DropDown, NUN_PROFESSIONS[ (locals.NuNDataPlayers[local_player.currentNote.unit].prof1) ]);
	elseif ( contact.prof1 ~= nil ) then
		locals.dropdownFrames.ddProf1 = contact.prof1;
		contact.prof1 = NUN_PROFESSIONS[locals.dropdownFrames.ddProf1];
		UIDropDownMenu_SetSelectedID(NuNProf1DropDown, locals.dropdownFrames.ddProf1);
		UIDropDownMenu_SetText(NuNProf1DropDown, contact.prof1);
	else
		UIDropDownMenu_ClearAll(NuNProf1DropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prof2 ~= nil ) then
		UIDropDownMenu_SetSelectedID(NuNProf2DropDown, locals.NuNDataPlayers[local_player.currentNote.unit].prof2);
		UIDropDownMenu_SetText(NuNProf2DropDown, NUN_PROFESSIONS[ (locals.NuNDataPlayers[local_player.currentNote.unit].prof2) ]);
	elseif ( contact.prof1 ~= nil ) then
		locals.dropdownFrames.ddProf2 = contact.prof2;
		contact.prof2 = NUN_PROFESSIONS[locals.dropdownFrames.ddProf2];
		UIDropDownMenu_SetSelectedID(NuNProf2DropDown, locals.dropdownFrames.ddProf2);
		UIDropDownMenu_SetText(NuNProf2DropDown, contact.prof2);
	else
		UIDropDownMenu_ClearAll(NuNProf2DropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].arena ~= nil ) then
		UIDropDownMenu_SetSelectedID(NuNArenaRDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].arena);
		UIDropDownMenu_SetText(NuNArenaRDropDown, NUN_ARENAR[ (locals.NuNDataPlayers[local_player.currentNote.unit].arena) ]);
	elseif ( contact.arena ~= nil ) then
		locals.dropdownFrames.ddArena = contact.arena;
		contact.arena = NUN_ARENAR[locals.dropdownFrames.ddArena];
		UIDropDownMenu_SetSelectedID(NuNArenaRDropDown, locals.dropdownFrames.ddArena);
		UIDropDownMenu_SetText(NuNArenaRDropDown, contact.arena);
	else
		UIDropDownMenu_ClearAll(NuNArenaRDropDown);
	end

	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].hrank ~= nil ) then
		UIDropDownMenu_SetSelectedID(locals.NuNHRankDropDown, locals.NuNDataPlayers[local_player.currentNote.unit].hrank);
		UIDropDownMenu_SetText(locals.NuNHRankDropDown, locals.Ranks[ (locals.NuNDataPlayers[local_player.currentNote.unit].hrank) ]);
	elseif ( contact.hrank ~= nil ) then
		locals.dropdownFrames.ddHRank = contact.hrank;
		contact.hrank = locals.Ranks[locals.dropdownFrames.ddHRank];
		UIDropDownMenu_SetSelectedID(locals.NuNHRankDropDown, locals.dropdownFrames.ddHRank);
		UIDropDownMenu_SetText(locals.NuNHRankDropDown, contact.hrank);
	else
		UIDropDownMenu_ClearAll(locals.NuNHRankDropDown);
	end

end

function NuNF.NuN_GNoteFromItem(link, theTT)
	local catTxt = "";

	catTxt = NuNF.NuN_ExtractTooltipInfo(catTxt, theTT);
	local_player.currentNote.general = link;
	contact.type = NuNGet_CommandID(NUN_NOTETYPES, "ITM");
	NuN_ShowTitledGNote(catTxt);
end

function NuNF.NuN_ExtractTooltipInfo(xTTText, theTT)
	local lftTxt, rgtTxt, needRight;
	local endLine = "\n";
	local tmpTxt;
	local ttLLen = NuNC.NUN_TT_LEN;

	if ( NuNSettings[local_player.realmName].ttLLen ) then
		if ( NuNSettings[local_player.realmName].ttLLen == "" ) then
			ttLLen = 0;
		else
			ttLLen = tonumber( NuNSettings[local_player.realmName].ttLLen );
		end
	end

	for i=2, 23, 1 do
		lftTxt = _G[theTT.."TextLeft"..i];
		if ( lftTxt ) then
			lftTxt = lftTxt:GetText();

			rgtTxt = _G[theTT.."TextRight"..i];
			if ( rgtTxt ) then rgtTxt = rgtTxt:GetText(); end

			needRight = false;
			tmpTxt = "";
			if ( lftTxt ) then
				if ( ( strfind(lftTxt, "\"" ) ) or ( ( strfind(lftTxt, "/") ) and ( strfind(lftTxt, "\)") ) ) ) then
					tmpTxt = NuNC.NUN_GOLD..lftTxt;
				elseif ( strfind(lftTxt, ":") ) then
					tmpTxt = NuNC.NUN_GREEN..lftTxt;
				else
					tmpTxt = NuNC.NUN_WHITE..lftTxt;
				end
				if ( rgtTxt ) then
					needRight = NuNF.NuN_TestLeftTT(lftTxt);
					if ( needRight ) then
						local lLen = strlen(lftTxt);
						local rLen = strlen(rgtTxt);
						local spaces = ttLLen - (lLen + rLen) - 10;
						local pad = strrep(" ", spaces);
						tmpTxt = tmpTxt..pad..rgtTxt;
					end
				end
				xTTText = xTTText..tmpTxt..NuNC.NUN_C_END..endLine;
			end
		end
	end

	return xTTText;
end

function NuNF.NuN_TestLeftTT(lftTxt)
	if 	( strfind(lftTxt, NUN_HAND ) ) or 
		( strfind(lftTxt, NUN_HAND2) ) or 
		( strfind(lftTxt, NUN_FEET ) ) or 
		( strfind(lftTxt, NUN_LEGS ) ) or 
		( strfind(lftTxt, NUN_HEAD ) ) or 
		( strfind(lftTxt, NUN_WAIST ) ) or 
		( strfind(lftTxt, NUN_SHOULDER ) ) or 
		( strfind(lftTxt, NUN_CHEST ) ) or 
		( strfind(lftTxt, NUN_WRIST ) ) or 
		( strfind(lftTxt, NUN_DAMAGE ) ) then
			return true;
	else
			return false;
	end
end

function NuNF.NuN_CheckPartyByName(parmN)
	local partym;
	local numParty = GetNumPartyMembers();

	for groupIndex = 1, numParty, 1 do
 		if (GetPartyMember(groupIndex)) then
 			partym = "party"..groupIndex;
 			local lName = UnitName(partym);
 			if ( lName == parmN) then
 				return partym;
 			end
 		end
	end
	return nil;
end

function NuNF.NuN_CheckRaidByName(parmN)
	local raidm;
	local lclName;
	local numRaid = GetNumRaidMembers();

	for raidIndex = 1, numRaid, 1 do
		lclName = GetRaidRosterInfo(raidIndex);
		if ( lclName == parmN ) then
			raidm = "raid"..raidIndex;
			return raidm;
		end
	end
	return nil;
end

function NuNF.NuN_CopyTable(fT, tT)
	if ( ( not fT ) or ( not tT ) or ( type(fT) ~= "table" ) or ( type(tT) ~= "table" ) ) then return -1; end

	for idx, value in pairs(fT) do
		if ( type(value) == "table" ) then
			tT[idx] = {};
			NuNF.NuN_CopyTable(value, tT[idx]);

		else
			tT[idx] = value;
		end
	end
end

function NuNF.QueryTalents()
	if ( not NuNTalents.player ) then
		NuNTalents = {};
		return;
	else
		NuNTalents.total = 0;
		NuNTalents.summary = "";
	end

	-- this flag controls whether the following code uses data from received from inspecting a player or whether it just uses our own data (for debugging)
	local useInspectInfo = true;
	NuNTalents.tabs = GetNumTalentTabs(useInspectInfo) or 0;
	
	-- return values from GetTalentTabInfo()
	local tabID, tabName, tabDesc, tabIcon, tabPointsSpent, tabPreviewPointsSpent, tabPointsAllocated, _, tabUnlocked;
	local prefix = "";
	
	-- return values from GetTalentInfo()
	local talentName, talentIcon, talentTier, talentColumn, talentRank, talentMaxRank, talentIsExceptional, previewRank, totalAllocatedRank;

	-- used to determine which tree the player is spec'd into.
	local primarySpec = { tabRef = nil, totalPoints = 0 }
	for _tab = 1, NuNTalents.tabs do
		-- record tab
		local talentGroupIndex = 1;	--@todo orgevo: need to check both specs (dual-spec)
		tabID, tabName, tabDesc, tabIcon, tabPointsSpent, _, tabPreviewPointsSpent, tabUnlocked = GetTalentTabInfo(_tab, useInspectInfo, false, talentGroupIndex);
		--nun_msgf("QueryTalents - tabID:%s  tabName:%s  tabPointsSpent:%s  tabPreviewPointsSpent:%s  tabUnlocked:%s  talentGroupIndex:%i",
		--	tostring(tabID), tostring(tabName), tostring(tabPointsSpent), tostring(tabPreviewPointsSpent), tostring(tabUnlocked), talentGroupIndex);

		if ( tabUnlocked ) then
			local tabPointsAllocated = tabPointsSpent + tabPreviewPointsSpent;

			-- check to see if this talent tree is the player's current spec
			if ( tabPointsAllocated > primarySpec.totalPoints ) then
				primarySpec.tabRef = tabID;
				primarySpec.totalPoints = tabPointsAllocated;
				
				NuNTalents.spec = tabName;
				NuNTalents.icon = tabIcon;
			end

			NuNTalents.summary = NuNTalents.summary .. prefix .. tabPointsAllocated;
			NuNTalents.total = NuNTalents.total + tabPointsAllocated;
			NuNTalents[_tab] = {};
			NuNTalents[_tab].spec = tabName;
			NuNTalents[_tab].points = tabPointsAllocated;
			NuNTalents[_tab].icon = tabIcon;
			NuNTalents[_tab].specifics = {};

			-- record talent choices
			local talentCount = GetNumTalents(_tab, useInspectInfo, false);
			for talentIndex = 1, talentCount do
				talentName, talentIcon, talentTier, talentColumn, talentRank, talentMaxRank, talentIsExceptional, _, previewRank = GetTalentInfo(_tab, talentIndex, useInspectInfo);
				totalAllocatedRank = talentRank;
				--[[
				commented out as previewRank always seems to have a value, regardless of whether the player has actually allocated points to the talent using the preview feature.
				if ( totalAllocatedRank and previewRank ) then
					totalAllocatedRank = totalAllocatedRank + previewRank;
				end
				--]]
				--nun_msgf("  >> >> talentName:%s  talentTier:%s  talentColumn:%s  talentRank:%s  talentMaxRank:%s  previewRank:%s  totalAllocatedRank:%s  exceptional:%s",
				--	tostring(talentName), tostring(talentTier), tostring(talentColumn), tostring(talentRank), tostring(talentMaxRank), tostring(previewRank), tostring(totalAllocatedRank), tostring(talentIsExceptional));
					
				if ( talentName and talentRank and talentRank > 0 ) then
					local talentData = {
						talentTier = talentTier,
						talentColumn = talentColumn,
						talentName = talentName,
						iconPath = talentIcon,
						curR = totalAllocatedRank,
						maxR = talentMaxRank,
						exceptional = talentIsExceptional,
					};
					tinsert(NuNTalents[_tab].specifics, talentData);
				end
			end
			prefix = "-";
		end
	end

	-- Live update if record for player already exists...
	if ( ( locals.NuNDataPlayers[NuNTalents.player] ) and ( NuNTalents.total > 0 ) ) then
		-- copy NuNTalents details to .talents array
		locals.NuNDataPlayers[NuNTalents.player].talents = {};
		NuNF.NuN_CopyTable(NuNTalents, locals.NuNDataPlayers[NuNTalents.player].talents);

	elseif ( locals.NuNDataPlayers[NuNTalents.player] ) then
		locals.NuNDataPlayers[NuNTalents.player].talents = nil;
	end

	NuNFrame:UnregisterEvent("INSPECT_READY");
end

-- 5.60 NuN_Target re-written to NuN_UnitInfo for adjusting Open Note details without (necessarily) saving to database
function NuNF.NuN_UnitInfo(unitTest, contactName, theUnitID)							-- 5.60 Allow passing of the unitID
	local lName;

	-- Try to fetch a valid unitID
	if ( not theUnitID ) then							-- 5.60 Changes in this if block
		lName = UnitName("target");
		if ( ( lName ) and ( lName == contactName ) ) then
			theUnitID = "target";
		end

		if ( not theUnitID ) then
			theUnitID = NuNF.NuN_CheckPartyByName(contactName);
		end

		if ( not theUnitID ) then
			theUnitID = NuNF.NuN_CheckRaidByName(contactName);
		end
	end

	-- if not just testing, and have a unitID, then fetch info
	if ( ( not unitTest ) and ( theUnitID ) ) then
		local lRace;
		local lClass;
		local lSex;
		local lPvPRank;
		local lPvPRankID;
		local lgName;
		local lgRank;
		local lgRankIndex;

		lRace = UnitRace(theUnitID);
		if ( lRace ~= nil ) then
			contact.race = lRace;
			locals.dropdownFrames.ddRace = NuNF.NuNGet_TableID(locals.Races, contact.race);
			UIDropDownMenu_SetSelectedID(locals.NuNRaceDropDown, locals.dropdownFrames.ddRace);
			UIDropDownMenu_SetText(locals.NuNRaceDropDown, contact.race);
		end

		lClass = UnitClass(theUnitID);
		if ( lClass ~= nil ) then
			contact.class = lClass;
			locals.dropdownFrames.ddClass = NuNF.NuNGet_TableID(locals.Classes, contact.class);
			UIDropDownMenu_SetSelectedID(locals.NuNClassDropDown, locals.dropdownFrames.ddClass);
			UIDropDownMenu_SetText(locals.NuNClassDropDown, contact.class);
		end

		lSex = UnitSex(theUnitID);
		if ( lSex ~= nil ) then
			if ( lSex == 2 ) then
				lsex = NUN_MALE;
			elseif ( lSex == 3 ) then
				lsex = NUN_FEMALE;
			end
			locals.dropdownFrames.ddSex = NuNF.NuNGet_TableID(NUN_SEXES, lsex);
			UIDropDownMenu_SetSelectedID(NuNSexDropDown, locals.dropdownFrames.ddSex);
			UIDropDownMenu_SetText(NuNSexDropDown, lsex);
		end

		lgName, lgRank, lgRankIndex = GetGuildInfo(theUnitID);
		if ( ( lgName ) and ( lgName ~= "" ) ) then
			contact.guild = lgName;

			bttnHeadingText1 = _G["NuNTitleButton1ButtonTextHeading"];
			bttnDetailText1 = _G["NuNInforButton1ButtonTextDetail"];
			bttnHeadingText2 = _G["NuNTitleButton2ButtonTextHeading"];
			bttnDetailText2 = _G["NuNInforButton2ButtonTextDetail"];

			if ( bttnHeadingText1:GetText() == NUN_DFLTHEADINGS[1] ) then
				bttnDetailText1:SetText(lgName);
				locals.bttnChanges[6] = lgName;
			end
			if ( bttnHeadingText2:GetText() == NUN_DFLTHEADINGS[2] ) then
				if ( lgRankIndex == 0 ) then
					lgRankTxt = ( "GM : "..lgRank );
				else
					lgRankTxt = ( lgRankIndex.." : "..lgRank );
				end
				bttnDetailText2:SetText(lgRankTxt);
				locals.bttnChanges[7] = lgRankTxt;
			end
		end

		if ( theUnitID == "target" ) then
			for idx = 1, 19, 1 do
				local text = GetInventoryItemLink(theUnitID, idx);
				if ( text ) then
					NuNText:SetText( NuNText:GetText() .. "\n" .. text );
				end
			end
		end

		if ( ( contactName ) and ( contactName ~= "" ) ) then
			if ( ( CheckInteractDistance(theUnitID, 1) )
			and ( CanInspect(theUnitID, true) )
			and ( ( UnitLevel(theUnitID) or 0 ) > 9 ) ) then

				NuNTalents = {};
				NuNTalents.player = contactName;
				NuNTalents.theUnitID = theUnitID;

				local inspected = "";
				if ( (InspectFrame) and ( InspectFrame:IsVisible() ) ) then
					inspected = InspectFrame.unit;
				end

				if ( inspected == contactName ) then
					NuNF.QueryTalents();

				else
					NuNFrame:RegisterEvent("INSPECT_READY");
					NotifyInspect(theUnitID);
				end

			else
				NuNTalents = {};
			end
		end

	end

	return theUnitID;
end

-- 5.60 New function for updating background database information based on unitID
function NuNF.NuN_UnitInfoDB(lMember, lUnit)
	if ( not locals.NuNDataPlayers[lMember] ) then
		return;
	end

	if ( NuN_horde ) then
		locals.NuNDataPlayers[lMember].race = NuNF.NuNGet_TableID( NUN_HRACES, UnitRace(lUnit) );	
	else                                        									
		locals.NuNDataPlayers[lMember].race = NuNF.NuNGet_TableID( NUN_ARACES, UnitRace(lUnit) );	
	end
		
	if ( NuN_horde ) then                       									
		locals.NuNDataPlayers[lMember].cls = NuNF.NuNGet_TableID( NUN_HCLASSES, UnitClass(lUnit) );	
	else                                        									
		locals.NuNDataPlayers[lMember].cls = NuNF.NuNGet_TableID( NUN_ACLASSES, UnitClass(lUnit) );	
	end                                         									

	local lSex = UnitSex( lUnit );                    								
	if ( lSex ) then                                        						
		locals.NuNDataPlayers[lMember].sex = lSex - 1;										
	end                                                     						

	local lGuild, lGRank, lGRankIndex = GetGuildInfo( lUnit );						
	local compoundKey, compoundSettingsKey, lGRankTxt;
	if ( ( lGuild ) and ( lGuild ~= "" ) ) then										
		-- Guild
		locals.NuNDataPlayers[lMember].guild = lGuild;
		settingsKey = locals.pHead .. "1";
		compoundKey = lMember .. settingsKey;
		if ( ( ( locals.NuNDataPlayers[compoundKey] ) and ( locals.NuNDataPlayers[compoundKey].txt ~= NUN_DFLTHEADINGS[1] ) ) or				
			 ( ( NuNSettings[local_player.realmName][settingsKey] ) and ( NuNSettings[local_player.realmName][settingsKey].txt ~= NUN_DFLTHEADINGS[1] ) ) ) then
		else
			compoundKey = lMember .. locals.pDetl .. "1";                        	
			locals.NuNDataPlayers[compoundKey] = {};									
			locals.NuNDataPlayers[compoundKey].txt = lGuild;							
		end
		-- Guild Rank
		settingsKey = locals.pHead .. "2";
		compoundKey = lMember .. settingsKey;
		if ( ( ( locals.NuNDataPlayers[compoundKey] ) and ( locals.NuNDataPlayers[compoundKey].txt ~= NUN_DFLTHEADINGS[2] ) ) or
			 ( ( NuNSettings[local_player.realmName][settingsKey] ) and ( NuNSettings[local_player.realmName][settingsKey].txt ~= NUN_DFLTHEADINGS[2] ) ) ) then
		else																		
			compoundKey = lMember .. locals.pDetl .. "2";
			if ( lGRankIndex == 0 ) then
				lGRank = ("GM : "..lGRank);										
			else																	
				lGRank = (lGRankIndex.." : "..lGRank);
			end
			locals.NuNDataPlayers[compoundKey] = {};
			locals.NuNDataPlayers[compoundKey].txt = lGRank;
		end                                                         				
	end																				
end

-- little utility function for copying a table of str
function NuNF.NuN_copyT(t1, t2, c_prefix)
	local i1 = getn(t1);
	for i2=1, getn(t2), 1 do		-- #t2
		i1 = i1 + 1;
		t1[i1] = c_prefix..t2[i2];
	end
end

-- Sort the Notes to display :
--	Players of your Faction first			- ofc if no Player notes have been included in the filter then....
--	Players of opposing Faction second		- ofc if no Player notes have been included in the filter than.....
--	General Notes last				- ofc if no General notes have been included in the filter then......
function NuNF.NuN_DefaultSort()
	if ( NuN_horde ) then
		NuNF.NuN_copyT(locals.foundNuN, locals.foundHNuN, NuNC.NUN_HORD_C);
		NuNF.NuN_copyT(locals.foundNuN, locals.foundANuN, NuNC.NUN_ALLI_C);
		NuNF.NuN_copyT(locals.foundNuN, locals.foundNNuN, NuNC.NUN_NOTE_C);
	else
		NuNF.NuN_copyT(locals.foundNuN, locals.foundANuN, NuNC.NUN_ALLI_C);
		NuNF.NuN_copyT(locals.foundNuN, locals.foundHNuN, NuNC.NUN_HORD_C);
		NuNF.NuN_copyT(locals.foundNuN, locals.foundNNuN, NuNC.NUN_NOTE_C);
	end
end

function NuNF.NuN_GetDateStamp()
	local dateStamp = date("%A, %d %B %Y  %H:%M:%S");
	dateStamp = NuNF.NuN_LocaliseDateStamp(dateStamp);
	return dateStamp;
end

-- this was used for usefully timestamping notes in the Saved Variables file
-- not seen by user, and no code depends on it yet, but might be useful
function NuNF.NuN_GetComparitiveDate(originalData)
	if ( ( suppressDateUpdate ) and ( originalData.lastChanged ) ) then
		return originalData.lastChanged;
	else
		local comparitiveDate = tonumber( date("%Y%m%d%H%M%S") );
		return comparitiveDate;
	end
end

-- Location Stamping of notes
function NuNF.NuN_GetLoc()
	local locData = locals.player_Name.."'s "..NUN_LOC.." : ";
	local myCID, myC, mySubZ, myZID, myZ, px, py, coords;
	local loc = false;

	myCID = GetCurrentMapContinent();
	if ( ( myCID ~= nil) and ( myCID > 0 ) ) then
		myC = locals.continents[myCID].name;
	end
	if ( myC ~= nil ) then
		locData = locData..myC..", ";
		loc = true;
	end

	myZ = GetZoneText();
	if ( ( myZ == nil ) or ( myZ == "" ) ) then
		myZID = GetCurrentMapZone();
		if ( ( myZID ~= nil ) and ( myCID ~= nil ) and ( myCID > 0 ) ) then
			myZ = locals.continents[myCID].zones[myZID];
		end
	end
	if ( ( myZ ~= nil ) and ( myZ ~= "" ) ) then
		locData = locData..myZ..", ";
	end


	mySubZ = GetSubZoneText();
	if ( ( mySubZ == nil ) or ( mySubZ == "" ) ) then
		mySubZ = GetMinimapZoneText();
	end
	if ( mySubZ ~= nil ) then
		locData = locData..mySubZ..", ";
		loc = true;
	end

	px, py = GetPlayerMapPosition("player");
    	if ( ( px ~= nil ) and ( py ~= nil ) ) then
        	coords = format("(%d, %d)", px * 100.0, py * 100.0);
		locData = locData..coords;
		loc = true; 
    	end
    	if ( loc == true ) then
		return locData;
	else
		return "";
	end
end

function NuNF.NuN_GetDisplayText(dText)
	dText = strgsub(dText, "¬n", "\n");
	dText = strgsub(dText, "¬q", "\"");
	dText = strgsub(dText, "¬s", "\\");
	dText = strgsub(dText, "\124\124", "|");
	dText = strgsub(dText, "|C", "|c");
	dText = strgsub(dText, "|R", "|r");
	dText = strgsub(dText, "||c", "|c");
	dText = strgsub(dText, "||r", "|r");
	
	return dText;
end

function NuNF.NuN_SetSaveText(dText)
	dText = strgsub(dText, "\n", "¬n");
	dText = strgsub(dText, "\"", "¬q");
	dText = strgsub(dText, "\\", "¬s");
	dText = strgsub(dText, "\124\124", "|");
	dText = strgsub(dText, "|C", "|c");
	dText = strgsub(dText, "|R", "|r");
	dText = strgsub(dText, "||c", "|c");
	dText = strgsub(dText, "||r", "|r");

	return dText;
end

function NuNF.NuN_GetCText(gLclName)
	local txtIndex;
	local catText;

	catText = "";
	if ( locals.NuNDataPlayers[gLclName][locals.txtTxt] ) then
		catText = locals.NuNDataPlayers[gLclName][locals.txtTxt];
	end
	for i = 1, NuNC.NUN_MAX_ADD_TXT, 1 do
		txtIndex = locals.txtTxt..i;
		if ( locals.NuNDataPlayers[gLclName][txtIndex] ) then
			catText = catText..locals.NuNDataPlayers[gLclName][txtIndex];
		end
	end

	catText = NuNF.NuN_GetDisplayText(catText);
	return catText;
end

function NuNF.NuN_GetGText(gLclNote)
	local txtIndex;
	local catText;

	catText = "";
	if ( ( NuNDataRNotes[gLclNote] ) and ( NuNDataRNotes[gLclNote][locals.txtTxt] ) ) then
		catText = NuNDataRNotes[gLclNote][locals.txtTxt];
	elseif ( ( NuNDataANotes[gLclNote] ) and ( NuNDataANotes[gLclNote][locals.txtTxt] ) ) then
		catText = NuNDataANotes[gLclNote][locals.txtTxt];
	end
	for i = 1, NuNC.NUN_MAX_ADD_TXT, 1 do
		txtIndex = locals.txtTxt..i;
		if ( ( NuNDataRNotes[gLclNote] ) and ( NuNDataRNotes[gLclNote][txtIndex] ) ) then
			catText = catText..NuNDataRNotes[gLclNote][txtIndex];
		elseif ( ( NuNDataANotes[gLclNote] ) and ( NuNDataANotes[gLclNote][txtIndex] ) ) then
			catText = catText..NuNDataANotes[gLclNote][txtIndex];
		end
	end

	catText = NuNF.NuN_GetDisplayText(catText);
	return catText;
end

-- contact.text must be pre-populated before calling this function
function NuNF.NuN_SetCText(sLclName)
	local cLower;
	local cUpper;
	local txtIndex;
	local tmpText = contact.text;

	contact.text = NuNF.NuN_SetSaveText(contact.text);
	contact.text_len = strlen(contact.text);
	if ( contact.text_len < NuNC.NUN_MAX_TXT_CHR ) then
		cUpper = contact.text_len;
	else
		cUpper = NuNC.NUN_MAX_TXT_CHR;
	end
	locals.NuNDataPlayers[sLclName][locals.txtTxt] = strsub(contact.text, 1, cUpper);
	for i = 1, NuNC.NUN_MAX_ADD_TXT, 1 do
		cLower = NuNC.NUN_MAX_TXT_CHR * i;
		txtIndex = locals.txtTxt..i;
		if ( contact.text_len > cLower ) then
			cLower = cLower + 1;
			cUpper = NuNC.NUN_MAX_TXT_CHR * ( i + 1 );
			if ( contact.text_len < cUpper ) then
				cUpper = contact.text_len;
			end
			locals.NuNDataPlayers[sLclName][txtIndex] = strsub(contact.text, cLower, cUpper);
		else
			locals.NuNDataPlayers[sLclName][txtIndex] = "";
		end
	end
	locals.NuNDataPlayers[sLclName].lastChanged = NuNF.NuN_GetComparitiveDate(locals.NuNDataPlayers[sLclName]);

	contact.text = tmpText;
end

-- local_player.currentNote.general and general.text must be pre-populated with noteName and text before calling this function
function NuNF.NuN_SetGText(saveLvl)
	local cLower;
	local cUpper;
	local txtIndex;
	local tmpText = general.text;

	general.text = NuNF.NuN_SetSaveText(general.text);
	general.text_len = strlen(general.text);
	if ( general.text_len < NuNC.NUN_MAX_TXT_CHR ) then
		cUpper = general.text_len;
	else
		cUpper = NuNC.NUN_MAX_TXT_CHR;
	end

	if ( saveLvl == "Realm" ) then
		NuNDataRNotes[local_player.currentNote.general][locals.txtTxt] = strsub(general.text, 1, NuNC.NUN_MAX_TXT_CHR);
		for i = 1, NuNC.NUN_MAX_ADD_TXT, 1 do
			cLower = NuNC.NUN_MAX_TXT_CHR * i;
			txtIndex = locals.txtTxt..i;
			if ( general.text_len > cLower ) then
				cLower = cLower + 1;
				cUpper = NuNC.NUN_MAX_TXT_CHR * ( i + 1 );
				if ( general.text_len < cUpper ) then
					cUpper = general.text_len;
				end
				NuNDataRNotes[local_player.currentNote.general][txtIndex] = strsub(general.text, cLower, cUpper);
			else
				NuNDataRNotes[local_player.currentNote.general][txtIndex] = "";
			end
		end
		NuNDataRNotes[local_player.currentNote.general].lastChanged = NuNF.NuN_GetComparitiveDate(NuNDataRNotes[local_player.currentNote.general]);
	else
		NuNDataANotes[local_player.currentNote.general][locals.txtTxt] = strsub(general.text, 1, NuNC.NUN_MAX_TXT_CHR);
		for i = 1, NuNC.NUN_MAX_ADD_TXT, 1 do
			cLower = NuNC.NUN_MAX_TXT_CHR * i;
			txtIndex = locals.txtTxt..i;
			if ( general.text_len > cLower ) then
				cLower = cLower + 1;
				cUpper = NuNC.NUN_MAX_TXT_CHR * ( i + 1 );
				if ( general.text_len < cUpper ) then
					cUpper = general.text_len;
				end
				NuNDataANotes[local_player.currentNote.general][txtIndex] = strsub(general.text, cLower, cUpper);
			else
				NuNDataANotes[local_player.currentNote.general][txtIndex] = "";
			end
		end
		NuNDataANotes[local_player.currentNote.general].lastChanged = NuNF.NuN_GetComparitiveDate(NuNDataANotes[local_player.currentNote.general]);
	end

	general.text = tmpText;
end

-- build the NotesUNeed tooltip based on the NotesUNeed note
function NuNF.NuN_BuildTT(nunTT)
	local lGuild = nil;
	local lGuildR = nil;
	local lprof = "";
	local lprating = nil;
	local tt = {};
	local tti = 0;
	local srchText, sStart, sStop, tipText;
	local lineCount = 0;
	local finalTipText;
	local txtLines;
	local NuN_trunc = false;
	local gttLines = 0;
	local ttLinesDiff = 0;
	local ttLen = NuNC.NUN_TT_MAX;
	local ttLLen = NuNC.NUN_TT_LEN;
	
	if ( ( NuNSettings[local_player.realmName].toolTips ) or ( nunTT == NuN_PinnedTooltip ) ) then
		if ( NuN_PinnedTooltip.type ~= "General" ) and ( NuN_PinnedTooltip.type ~= "QuestHistory" ) and ( locals.NuNDataPlayers[locals.ttName] ) then
			local isIgnored = NuNF.NuN_IsPlayerIgnored(locals.ttName);
			if ( ( NuNSettings[local_player.realmName].hignores ) and ( isIgnored ) ) then
--				nun_msgf("Currently ignoring %s - not displaying tooltip for player because tooltips and notes for ignored players has been disabled in the options.", locals.ttName);
				return;
			end
			nunTT:AddLine(NuN_Strings.NUN_NOTESUNEED_INFO .. NuNC.NUN_PINNED_TT_PADDING, 1, 0.7, 0);
			lineCount = lineCount + 1;

			-- if we have talent information for this player, inject it into the tooltip now
			local playerTalents = locals.NuNDataPlayers[locals.ttName].talents;
			if ( playerTalents and playerTalents.total > 0 ) then
				local tooltipTalentSpecLine = "";
				-- if we were able to determine the player's spec and recorded the spec's icon, include that too for that extra lit bit of snazz
				if ( playerTalents.icon ) then
					tooltipTalentSpecLine = tooltipTalentSpecLine .. "|T" .. playerTalents.icon .. ":0|t ";
				end
				tooltipTalentSpecLine = tooltipTalentSpecLine .. playerTalents.spec;
				nunTT:AddDoubleLine(tooltipTalentSpecLine, playerTalents.summary, nil, nil, nil, 0, 1, 0);
			end

			srchText = NuNF.NuN_GetCText(locals.ttName);

			local alts, counter = "", 0;
			local __, found, alt = strfind(srchText, "<ALT:(%a+)>");
			while ( alt ) do
				counter = counter + 1;
				alts = alts .. alt;
				__, found, alt = strfind(srchText, "<ALT:(%a+)>", found);
				if ( alt ) then
					alts = alts .. ", ";
					if ( counter > 3 ) then
						alts = alts .. "\n";
						counter = 0;
					end
				end
			end
			if ( alts ~= "" ) then
				nunTT:AddLine(alts, 0.1, 0.9, 0.1);
				srchText = strgsub(srchText, "<ALT:%a+>\n", "");
				srchText = strgsub(srchText, "<ALT:%a+>,%s*", "");
				srchText = strgsub(srchText, "<ALT:%a+>", "");
			end

			if ( NuN_State.NuN_PinUpHeader == true ) then
				nunTT:AddLine(locals.ttName);
				lineCount = lineCount + 1;
			end

			for n = 1, locals.uBttns, 1 do
				locals.headingNumber = locals.pHead .. n;
				locals.headingName = locals.ttName..locals.headingNumber;
				locals.headingDate = locals.ttName .. locals.pDetl .. n;
				if ( ( (n == 1) and (not locals.NuNDataPlayers[locals.headingName]) ) or ( (n==1) and ( locals.NuNDataPlayers[locals.headingName].txt == NUN_DFLTHEADINGS[n] ) ) ) then
					if ( locals.NuNDataPlayers[locals.headingDate] ) then
						lGuild = locals.NuNDataPlayers[locals.headingDate].txt;
					end
				elseif ( ( (n == 2) and (not locals.NuNDataPlayers[locals.headingName]) ) or ( (n==2) and ( locals.NuNDataPlayers[locals.headingName].txt == NUN_DFLTHEADINGS[n] ) ) ) then
					if ( locals.NuNDataPlayers[locals.headingDate] ) then
						lGuildR = locals.NuNDataPlayers[locals.headingDate].txt;
					end
				elseif ( locals.NuNDataPlayers[locals.headingName] ) then
					local ttDetl = (strlower(locals.NuNDataPlayers[locals.headingName].txt));
					if ( strfind(ttDetl, NuNC.NUN_TT_HDNG) ) then
						if ( locals.NuNDataPlayers[locals.headingDate] ) then
							tti = tti + 1;
							tt[tti] = locals.NuNDataPlayers[locals.headingDate].txt;
						end
					end
				elseif ( NuNSettings[local_player.realmName][locals.headingNumber] ) then
					local ttDetl = (strlower(NuNSettings[local_player.realmName][locals.headingNumber].txt));
					if ( strfind(ttDetl, NuNC.NUN_TT_HDNG) ) then
						if ( locals.NuNDataPlayers[locals.headingDate] ) then
							tti = tti + 1;
							tt[tti] = locals.NuNDataPlayers[locals.headingDate].txt;
						end
					end
				end
			end

			for i = 1, getn(tt), 1 do				-- #tt
				nunTT:AddLine(tt[i], 0.9, 0.2, 0.2);
				lineCount = lineCount + 1;
			end

			if ( ( lGuild ~= nil) and ( lGuild ~= "" ) ) then
				if ( ( lGuildR ~= nil ) and ( lGuildR ~= "" ) ) then
					lGuild = lGuild.." : "..lGuildR;
				end
				nunTT:AddLine(lGuild, 0.9, 0.9, 0);
				lineCount = lineCount + 1;
			end

			if ( locals.NuNDataPlayers[locals.ttName].prating ) then
				lprating = NuNSettings.ratings[ locals.NuNDataPlayers[locals.ttName].prating ];
				nunTT:AddLine(lprating, 0.7, 0.7, 0.9);
				lineCount = lineCount + 1;
			end

			if ( locals.NuNDataPlayers[locals.ttName].prof1 ) then
				lprof = NUN_PROFESSIONS[locals.NuNDataPlayers[locals.ttName].prof1];
			end
			if ( locals.NuNDataPlayers[locals.ttName].prof2 ) then
				if (lprof == "") then
					lprof = NUN_PROFESSIONS[locals.NuNDataPlayers[locals.ttName].prof2];
				else
					lprof = lprof.." - "..NUN_PROFESSIONS[locals.NuNDataPlayers[locals.ttName].prof2];
				end
			end
			if ( lprof ~= "" ) then
				nunTT:AddLine(lprof, 0.8, 0.2, 0.8);
				lineCount = lineCount + 1;
			end

		elseif ( ( NuN_PinnedTooltip.type ~= "Contact" ) and ( NuN_PinnedTooltip.type ~= "QuestHistory" ) and ( ( NuNDataRNotes[locals.ttName] ) or ( NuNDataANotes[locals.ttName] ) ) ) then
			nunTT:AddLine(NuN_Strings.NUN_NOTESUNEED_INFO .. NuNC.NUN_PINNED_TT_PADDING, 1, 0.7, 0);
			lineCount = lineCount + 1;

			if ( NuN_State.NuN_PinUpHeader == true ) then
				nunTT:AddLine(locals.ttName);
				lineCount = lineCount + 1;
			end

			srchText = NuNF.NuN_GetGText(locals.ttName);
		elseif ( ( NuN_PinnedTooltip.type == "QuestHistory" ) and ( NuNQuestHistory[locals.ttName] ) ) then
			nunTT:AddLine(NuN_Strings.NUN_NOTESUNEED_INFO .. NuNC.NUN_PINNED_TT_PADDING, 1, 0.7, 0);
			lineCount = lineCount + 1;

			if ( NuN_State.NuN_PinUpHeader == true ) then
				nunTT:AddLine(locals.ttName);
				lineCount = lineCount + 1;
			end

			srchText = NuNF.NuN_GetDisplayText( NuNQuestHistory[locals.ttName].txt );
		end

		if ( NuN_PinnedTooltip.type ~= "QuestHistory" ) then
			if ( NuNSettings[local_player.realmName].ttLen ) then
				if ( NuNSettings[local_player.realmName].ttLen == "" ) then
					ttLen = 0;
				else
					ttLen = tonumber( NuNSettings[local_player.realmName].ttLen );
				end
			end
			if ( NuNSettings[local_player.realmName].ttLLen ) then
				if ( NuNSettings[local_player.realmName].ttLLen == "" ) then
					ttLLen = 0;
				else
					ttLLen = tonumber( NuNSettings[local_player.realmName].ttLLen );
				end
			end
		end

		if ( ( NuN_State.NuN_MouseOver == true ) and ( NuNSettings[local_player.realmName].minOver ) and ( not IsAltKeyDown() ) ) then
			srchText = "";
		end

		if ( ( srchText ~= nil ) and ( srchText ~= "" ) ) then
			sStart = strfind(srchText, NuNC.NUN_TT_KEYPHRASE);
			if ( sStart ~= nil ) then
				sStart = sStart + NuNC.NUN_TT_KEYPHRASE_LEN;
				sStop = strfind(srchText, NuNC.NUN_TT_END, sStart);
				if ( sStop == nil ) then
					sStop = sStart + ttLen - 1;
				else
					sStop = sStop - 1;
					local sDiff = sStop - sStart;
					if ( ( sDiff > ttLen ) or ( sDiff < 0 ) ) then
						sStop = sStart + ttLen - 1;
						NuN_trunc = true;
					end
				end
				tipText = strsub(srchText, sStart, sStop);
			else
				if ( strlen(srchText) > ttLen ) then
					NuN_trunc = true;
				end
				tipText = strsub(srchText, 1, ttLen);
			end
			if ( strlen(tipText) > ttLLen ) then
				finalTipText, txtLines = NuNF.NuN_ParseTT(tipText, ttLLen);
			else
				finalTipText = tipText;
				txtLines = 1;
			end
			if ( NuN_trunc == true ) then
				finalTipText = finalTipText..NuNC.NUN_TT_ETC;
			end
			nunTT:AddLine(finalTipText, 0, 1, 0);
			lineCount = lineCount + txtLines;
		end

		local gttLines = GameTooltip:NumLines();
		local ttLinesDiff = lineCount - gttLines;
		if ( ttLinesDiff > NuNC.NUN_TT_LINES_TRIGGER ) then
			local scaleUp = math.floor( 4 * (NuNSettings[local_player.realmName].tScale - 1) );
			locals.NuN_TT_Y_Offset = ( ( ttLinesDiff - NuNC.NUN_TT_LINES_TRIGGER ) * ( NuNC.NUN_TT_Y_SHIFT + scaleUp ) );
			locals.NuN_TT_Y_Offset = math.floor( locals.NuN_TT_Y_Offset );
		end
	end
end

-- NotesUNeed allows options to control how long tooltip lines are, and how long they should be in total
function NuNF.NuN_ParseTT(txtIn, fragLen)
	local p1 = 0;
	local p2 = 0;
	local txtOut = "";
	local txtTmp = "";
	local txtTmpL = "";
	local xtraLines = "";
	local parsedLines = 1;

	while ( true ) do
		p2 = strfind(txtIn, "\n", ( p1 + 1 ) );
		if ( p2 == nil ) then
			break;
		end
		txtTmp = strsub(txtIn, ( p1 + 1 ), p2);
		txtTmpL = strlen(txtTmp);
		if ( txtTmpL > ( fragLen + 10 ) ) then
			txtTmp, xtraLines = NuNF.NuN_Fragment(txtTmp, fragLen);
		else
			xtraLines = 1;
		end
		p1 = p2;
		parsedLines = parsedLines + xtraLines;
		txtOut = txtOut..txtTmp;
	end
	txtTmp = strsub(txtIn, ( p1 + 1 ) );
	txtTmpL = strlen(txtTmp);
	if ( txtTmpL > fragLen ) then
		txtTmp, xtraLines = NuNF.NuN_Fragment(txtTmp, fragLen);
	else
		xtraLines = 1;
	end
	parsedLines = parsedLines + xtraLines;
	txtOut = txtOut..txtTmp;

	return txtOut, parsedLines;
end

-- Break up text in to lines no longer than that specified by the NuN Tooltip line length option
function NuNF.NuN_Fragment(txtWhole, fragLen)
	local p1 = 0;
	local p2 = 0;
	local lst = 0;
	local txtFrags = "";
	local count = 1;

	while ( true ) do
		p2 = strfind(txtWhole, " ", ( p1 + 1 ) )
		if ( p2 == nil ) then
			break
		end
		if ( p2 > ( lst + fragLen ) ) then
			lst = p1;
			txtFrags = txtFrags.."\n";
			count = count + 1;
		end
		txtFrags = txtFrags..strsub(txtWhole, ( p1 + 1 ), p2);
		p1 = p2;
	end
	txtFrags = txtFrags..strsub(txtWhole, ( p1 + 1 ) );

	return txtFrags, count;
end

function NuNF.NuN_HordeSetup()
	locals.NuNRaceDropDown = NuNHRaceDropDown;
	locals.NuNClassDropDown = NuNHClassDropDown;
	locals.NuNHRankDropDown = NuNHHRankDropDown;
	NuNARaceDropDown:Hide();
	NuNHRaceDropDown:Show();
	NuNAClassDropDown:Hide();
	NuNHClassDropDown:Show();
	NuNAHRankDropDown:Hide();
	NuNHHRankDropDown:Show();
	locals.Classes = NUN_HCLASSES;
	locals.Races = NUN_HRACES;
	locals.Ranks = NUN_HRANKS;
	NuNAFlag:Hide();
	NuNHFlag:Show();
end

function NuNF.NuN_AllianceSetup()
	locals.NuNRaceDropDown = NuNARaceDropDown;
	locals.NuNClassDropDown = NuNAClassDropDown;
	locals.NuNHRankDropDown = NuNAHRankDropDown;
	NuNHRaceDropDown:Hide();
	NuNARaceDropDown:Show();
	NuNHClassDropDown:Hide();
	NuNAClassDropDown:Show();
	NuNHHRankDropDown:Hide();
	NuNAHRankDropDown:Show();
	locals.Classes = NUN_ACLASSES;
	locals.Races = NUN_ARACES;
	locals.Ranks = NUN_ARANKS;
	NuNHFlag:Hide();
	NuNAFlag:Show();
end

function NuNF.NuN_GetSimpleName(cmplxName)
	local smplName, posB, posE;

	posB = strfind(cmplxName, "|h");
	posE = strfind(cmplxName, "]|h");
	if ( posB ~= nil ) and ( posE ~= nil ) and ( posB < posE ) then
		smplName = strsub(cmplxName, (posB + 3), (posE - 1));
		return smplName
	else
		return nil;
	end
end


function NuNF.NuN_BuildQuestText()
	local numQuestRewards, numQuestChoices, numQuestSpellRewards;
	local QuestRewardMoney, QuestRequiredMoney;
	local qText = "\n";
	local obj, objTxt, objType, itm, itmTxt;
	local gold, silver, copper, moneyTxt;
	local tmpQText1 = "";
	local tmpQText2 = "";
	local questItem = "QuestLogItem";
	local error = false;

	numQuestRewards = GetNumQuestLogRewards();  --Number of quest rewards
	numQuestChoices = GetNumQuestLogChoices();  --Number of quest choices
	if ( GetQuestLogRewardSpell() ) then
		numQuestSpellRewards = 1;
	end
	QuestRewardMoney = GetQuestLogRewardMoney();
	QuestRequiredMoney = GetQuestLogRequiredMoney();

	tmpQText1, tmpQText2 = GetQuestLogQuestText();
	qText = tmpQText2.."\n\n";

	if ( GetQuestLogTimeLeft() ) then
		qText = qText.."\nTimed Quest\n";
	end

	if ( QuestRequiredMoney ) and ( QuestRequiredMoney > 0 ) then
		if ( QuestRequiredMoney > 9999 ) then
			gold = ( QuestRequiredMoney / 10000 );
			gold = strformat("%d", gold);
			QuestRequiredMoney = QuestRequiredMoney - ( gold * 10000 );
		else
			gold = 0;
		end
		if ( QuestRequiredMoney > 99 ) then
			silver = ( QuestRequiredMoney / 100 );
			silver = strformat("%d", silver);
			QuestRequiredMoney = QuestRequiredMoney - ( silver * 100 );
		else
			silver = 0;
		end
		copper = QuestRequiredMoney;
		QuestRequiredMoneyTxt = strformat("\n%dg %ds %dc", gold, silver, copper);
		qText = qText..QuestRequiredMoneyTxt.."\n\n";
	end

	qText = qText.."\n\n"..tmpQText1.."\n";

	if ( ( QuestRewardMoney ) and ( QuestRewardMoney > 0 ) ) or ( numQuestRewards > 0 ) then
		qText = qText.."\n".. NuNC.NUN_REWARDS .."\n";
	end

	if ( QuestRewardMoney ) and ( QuestRewardMoney > 0 ) then
		QuestRewardMoneyTxt = NuN_BuildMoneyString(QuestRewardMoney);
		qText = qText..QuestRewardMoneyTxt.."\n";
	end

	--for i=1, numQuestRewards, 1 do
	--	bttn = _G["QuestLogItem"..i + numQuestChoices];
	--	if ( bttn.type ) then
	--		local link = GetQuestLogItemLink(bttn.type, bttn:GetID());
	--		if ( link ) then
	--			qText = qText..link.."\n";
	--		else
	--			link = GetQuestLogItemLink(bttn.type, ( i + numQuestChoices ) );
	--			if ( link ) then
	--				qText = qText..link.."\n";
	--			else
	--				error = true;
	--				qText = NUN_SLOWSERVER.." : NuN Err03";
	--				return qText, error;
	--			end
	--		end
	--	end
	--end
	
	for i=1, numQuestRewards, 1 do
		local link = GetQuestLogItemLink("reward", i);
		if ( link ) then
			qText = qText..link.."\n";
		else
			error = true;
			qText = NUN_SLOWSERVER.." : NuN Err03";
			return qText, error;
		end
	end
	
	if ( numQuestChoices > 1 ) then
		qText = qText.."\n".. NuNC.NUN_CHOICES .."\n";
	end
	
	for i=1, numQuestChoices, 1 do
		local link = GetQuestLogItemLink("choice", i)
		if ( link ) then
			if ( i > 1) then
				qText = qText.." / "..link;
			else
				qText = qText..link;
			end
		else
			error = true;
			qText = NUN_SLOWSERVER.." : NuN Err03";
			return qText, error;
		end
	end
	
	--for i=1, numQuestChoices, 1 do
	--	bttn = _G["QuestLogItem"..i];
	--	if ( bttn.type ) then
	--		local link = GetQuestLogItemLink(bttn.type, bttn:GetID());
	--		if ( link ) then
	--			if ( i > 1 ) then
	--				qText = qText.." / "..link;
	--			else
	--				qText = qText..link;
	--			end
	--		else
	--			link = GetQuestLogItemLink(bttn.type, i);
	--			if ( link ) then
	--				if ( i > 1 ) then
	--					qText = qText.." / "..link;
	--				else
	--					qText = qText..link;
	--				end
	--			else
	--				error = true;
	--				qText = NUN_SLOWSERVER.." : NuN Err03";
	--				return qText, error;
	--			end
	--		end
	--	end
	--end

	if ( qText ) then
		qText = NuNF.NuN_CleanQuestText(qText);
		if ( not qText ) then
			qText = "";
		end
	end

	return qText, error;
end

function NuNF.NuN_CheckQuestList(findName)
	local qTitle, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete;
	local foundIndex = -1;
	local rLevel, rTag, rComplete;

	locals.NuNQuestLog = {};

	for i = 1, GetNumQuestLogEntries(), 1 do
		qTitle, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete = GetQuestLogTitle(i);
		if ( ( qTitle ) and ( not qHeader ) ) then
			locals.NuNQuestLog[qTitle] = 1;
			if ( ( findName ) and ( findName == qTitle ) ) then
				foundIndex = i;
				rLevel = qLevel;
				rTag = qTag;
				rComplete = qComplete;
			end
		end
	end

	return foundIndex, rLevel, rTag, rComplete;
end

function NuNF.NuN_UpdateQuestNotes(qEvent)
if ron_disabled then
	local quest, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete;

	local previousQ = GetQuestLogSelection();

	locals.qTriggs = 0;

	if ( local_player.factionName ) then
		for qI=1, GetNumQuestLogEntries(), 1 do
			quest, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete = GetQuestLogTitle(qI);
			if ( ( quest ) and ( not qHeader ) ) then
				NuNF.NuN_ProcessQuest(quest, qLevel, qTag, qHeader, qCollapsed, qComplete, qI, qEvent);
			end
		end
	end

	if ( locals.qTriggs > 2 ) then
		locals.timeSinceLastUpdate = 0;
		NuN_State.NuN_QuestsUpdating = true;
	end

	if ( previousQ > 0 ) then
		SelectQuestLogEntry(previousQ);
	end
end
end

function NuNF.NuN_ProcessQuest(quest, qLevel, qTag, qHeader, qCollapsed, qComplete, qI, qEvent)
if ron_disabled then
	local saveLvl = nil;
	local qText;
	local location = NuNF.NuN_GetLoc();
	local pNuNQuestHistory = NuNData[local_player.realmName].QuestHistory[locals.player_Name];
	
	local l_c_note = local_player.currentNote.general;
	local l_g_text = general.text;
	local l_c_name = local_player.currentNote.unit;

	if ( ( pNuNQuestHistory[quest] ) and ( pNuNQuestHistory[quest].abandoned ) ) then
		pNuNQuestHistory[quest].abandoned = nil;
		return;
	end

	SelectQuestLogEntry(qI);

	local qChar = NuN_CheckTarget();
	if ( qChar == "N" ) then
		qChar = local_player.currentNote.general;
	else
		qChar = "";
	end
	if ( ( not pNuNQuestHistory[quest] ) and ( NuNSettings[local_player.realmName].autoQ ) ) then
		pNuNQuestHistory[quest] = {};
		pNuNQuestHistory[quest].sortDate = tostring(date("%Y%m%d%H%M%S"))..":"..qI;
		pNuNQuestHistory[quest].pLevel = UnitLevel("player");
		local qTxt = NuNC.NUN_CREATED.."   "..qChar.."\n    "..NuNF.NuN_GetDateStamp().."\n    "..location.."\n";
		pNuNQuestHistory[quest].txt = NuNF.NuN_SetSaveText(qTxt);
		locals.qTriggs = locals.qTriggs + 1;
	end

	local chk = NuNF.NuN_CleanQuestText( GetQuestLogQuestText() );
	if ( qLevel == nil ) then
		qLevel = "--";
	end
	if ( qTag == nil ) then
		qTag = "";
	end
	if ( qChar ~= "" ) then
		qChar = qChar.."   "..NuN_LocStrip(location);
	end
	qText = "\n"..quest.."     "..NUN_QLVL..qLevel.."     "..qTag.."\n"..qChar.."\n\n"..NuNF.NuN_BuildQuestText().."\n";

	if ( chk ) and ( not strfind(chk, UNKNOWN) ) then
		local testComplete = nil;
		if ( ( pNuNQuestHistory[quest] ) and ( pNuNQuestHistory[quest].complete ) ) then
			testComplete = NuNF.NuN_GetDisplayText(pNuNQuestHistory[quest].complete);
		end
		if ( ( qComplete ) and ( pNuNQuestHistory[quest] ) and ( ( not testComplete ) or ( ( testComplete ~= chk ) and ( strfind(qText, chk) ) ) ) ) then
			pNuNQuestHistory[quest].complete = NuNF.NuN_SetSaveText(chk);
			local qTxt = NuNF.NuN_GetDisplayText( pNuNQuestHistory[quest].txt );
			qTxt = qTxt .. "\n\n".. NuNC.NUN_COMPLETE .."\n    "..NuNF.NuN_GetDateStamp().."\n    "..location.."\n";
			pNuNQuestHistory[quest].txt = NuNF.NuN_SetSaveText(qTxt);
		end
		if ( NuNDataANotes[quest] ) then
			local_player.currentNote.general = quest;
			general.text = NuNF.NuN_GetGText(local_player.currentNote.general);
			if ( ( not strfind( general.text, chk) ) and ( strfind(qText, chk) ) ) then
				if ( ( NuNGNoteFrame:IsVisible() ) and ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "QST" ) ) then
					NuNGNoteFrame:Hide();
				end
				general.text = general.text.."\n\n".."--------------".."\n\n"..qText;
				NuNF.NuN_SetGText("Account");
			end
		elseif ( NuNDataRNotes[quest] ) then
			local_player.currentNote.general = quest;
			general.text = NuNF.NuN_GetGText(local_player.currentNote.general);
			if ( ( not strfind( general.text, chk ) ) and ( strfind(qText, chk) ) ) then
				if ( ( NuNGNoteFrame:IsVisible() ) and ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "QST" ) ) then
					NuNGNoteFrame:Hide();
				end
				general.text = general.text.."\n\n".."--------------".."\n\n"..qText;
				NuNF.NuN_SetGText("Realm");
			end
		elseif ( ( not NuNDataANotes[quest] ) and ( not NuNDataRNotes[quest] ) and ( NuNSettings[local_player.realmName].autoQ ) ) then
			if ( NuNSettings[local_player.realmName].dLevel ) then
				NuNDataANotes[quest] = {};
				NuNDataANotes[quest].type = 5;
				saveLvl = "Account";
			else
				NuNDataRNotes[quest] = {};
				NuNDataRNotes[quest].type = 5;
				saveLvl = "Realm";
			end
			local_player.currentNote.general = quest;
			general.text = qText;
			NuNF.NuN_SetGText(saveLvl);
			if ( ( qChar ~= "" ) and ( NuNSettings[local_player.realmName].autoMapNotes ) and ( qEvent == "Accepted" ) ) then
--				NuN_MapNote("Target", NUN_QUEST_GIVER, "", 9);
			end
		end
	end

	local_player.currentNote.general = l_c_note;
	general.text = l_g_text;
	local_player.currentNote.unit = l_c_name;
end
end

function NuNF.NuN_InitialiseSavedVariables()
	local idx, value;
	local_player.realmName = GetCVar("realmName");

	NuNData = _G.NuNData;
	NuNSettings = _G.NuNSettings;
	
	if ( not NuNData[local_player.realmName] ) then
		NuNData[local_player.realmName] = {};
		NuNSettings[local_player.realmName] = {};
		NuNSettings[local_player.realmName].toolTips = "1";
		NuNSettings[local_player.realmName].pScale = 1.00;
		NuNSettings[local_player.realmName].tScale = 1.00;
		NuNSettings[local_player.realmName].mScale = 1.00;
		NuNSettings[local_player.realmName].ttLen = NuNC.NUN_TT_MAX;
		NuNSettings[local_player.realmName].ttLLen = NuNC.NUN_TT_LEN;
		NuNData[local_player.realmName][locals.Notes_dbKey] = {};
		NuNSettings[local_player.realmName].dLevel = "1";
	end

	if ( not NuNSettings ) then
		_G.NuNSettings = {};
		NuNSettings = _G.NuNSettings;
	end

	if ( not NuNSettings[local_player.realmName] ) then
		NuNSettings[local_player.realmName] = {};
		NuNSettings[local_player.realmName].toolTips = "1";
		NuNSettings[local_player.realmName].pScale = 1.00;
		NuNSettings[local_player.realmName].tScale = 1.00;
		NuNSettings[local_player.realmName].mScale = 1.00;
		NuNSettings[local_player.realmName].ttLen = NuNC.NUN_TT_MAX;
		NuNSettings[local_player.realmName].ttLLen = NuNC.NUN_TT_LEN;
		NuNSettings[local_player.realmName].dLevel = "1";
	end

	if ( not NuNSettings.Version ) then
		for idx, value in pairs(NuNSettings) do
			if ( idx ~= "Version" ) then
				NuNSettings[idx].toolTips = "1";
				NuNSettings[idx].pScale = 1.00;
				NuNSettings[idx].tScale = 1.00;
				NuNSettings[idx].mScale = 1.00;
				NuNSettings[idx].ttLen = NuNC.NUN_TT_MAX;
				NuNSettings[idx].ttLLen = NuNC.NUN_TT_LEN;
				NuNSettings[local_player.realmName].dLevel = "1";
			end
		end
		NuNSettings.Version = NUN_VERSION;
		NuN_DataFix1();
	end

	if ( NuNSettings.Version < "2.51" ) then
		for idx, value in pairs(NuNSettings) do
			if ( idx ~= "Version" ) then
				NuNSettings[idx].mScale = 1.00;
			end
		end
		NuN_DataFix1();
		NuN_ResetFriendlyData(true, nil);
	elseif ( NuNSettings.Version < "3.00" ) then
		for idx, value in pairs(NuNSettings) do
			if ( idx ~= "Version" ) then
				NuNSettings[idx].mScale = 1.00;
			end
		end
		NuN_DataFix1();
		NuN_ResetFriendlyData(true, nil);
	elseif ( NuNSettings.Version < "4.25" ) then
		NuN_DataFix1();
		NuN_ResetFriendlyData(true, nil);
	elseif ( NuNSettings.Version < "5.00" ) then
		NuN_ResetFriendlyData(true, nil);
	end

	if ( NuNSettings.Version < NUN_VERSION ) then
		NuNSettings.Version = NUN_VERSION;
	end

	if ( not NuNData[locals.itmIndex_dbKey] ) then
		NuNData[locals.itmIndex_dbKey] = {};
	end

	if ( not NuNData[locals.Notes_dbKey] ) then
		NuNData[locals.Notes_dbKey] = {};
	end

	if ( not NuNSettings[local_player.realmName].gNotFriends ) then
		NuNSettings[local_player.realmName].gNotFriends = {};
	end

	if ( not NuNSettings[local_player.realmName].gNotIgnores ) then
		NuNSettings[local_player.realmName].gNotIgnores = {};
	end

	if ( NuNSettings[local_player.realmName].mapNotesPlayedVersions ) then
		NuNSettings[local_player.realmName].mapNotesPlayedVersions = nil;
	end

	if ( not NuNData[local_player.realmName].QuestHistory ) then
		NuNData[local_player.realmName].QuestHistory = {};
	end

	if ( not NuNData[local_player.realmName].QuestHistory[locals.player_Name] ) then
		NuNData[local_player.realmName].QuestHistory[locals.player_Name] = {};
	end

	if ( not NuNData[locals.mrgIndex_dbKey] ) then
		NuNData[locals.mrgIndex_dbKey] = {};
	end

	if ( not NuNData.dbg ) then
		NuNData.dbg = {};
	end

	if ( ( not NuNSettings[local_player.realmName].modifier ) or ( NuNSettings[local_player.realmName].modifier == "1" ) ) then
		NuNSettings[local_player.realmName].modifier = "on";
	end

	if ( NuNSettings[local_player.realmName].delay ) then
		NuN_DTrans.tDelay = NuNSettings[local_player.realmName].delay;
	end

	if ( not NuNSettings[local_player.realmName].cc1 ) then
		NuNSettings[local_player.realmName].cc1 = NuNC.NUN_CPRESETS[1];
	end
	if ( not NuNSettings[local_player.realmName].gc1 ) then
		NuNSettings[local_player.realmName].gc1 = NuNC.NUN_CPRESETS[1];
	end
	if ( not NuNSettings[local_player.realmName].cc2 ) then
		NuNSettings[local_player.realmName].cc2 = NuNC.NUN_CPRESETS[2];
	end
	if ( not NuNSettings[local_player.realmName].gc2 ) then
		NuNSettings[local_player.realmName].gc2 = NuNC.NUN_CPRESETS[2];
	end
	if ( not NuNSettings[local_player.realmName].cc3 ) then
		NuNSettings[local_player.realmName].cc3 = NuNC.NUN_CPRESETS[3];
	end
	if ( not NuNSettings[local_player.realmName].gc3 ) then
		NuNSettings[local_player.realmName].gc3 = NuNC.NUN_CPRESETS[3];
	end
	if ( not NuNSettings[local_player.realmName].cc4 ) then
		NuNSettings[local_player.realmName].cc4 = NuNC.NUN_CPRESETS[4];
	end
	if ( not NuNSettings[local_player.realmName].gc4 ) then
		NuNSettings[local_player.realmName].gc4 = NuNC.NUN_CPRESETS[4];
	end
	if ( not NuNSettings[local_player.realmName].cc5 ) then
		NuNSettings[local_player.realmName].cc5 = NuNC.NUN_CPRESETS[5];
	end
	if ( not NuNSettings[local_player.realmName].gc5 ) then
		NuNSettings[local_player.realmName].gc5 = NuNC.NUN_CPRESETS[5];
	end
	
	if ( NuNSettings[local_player.realmName].debugMode ) then
		locals.NuNDebug = NuNSettings[local_player.realmName].debugMode;
	else
		NuNSettings[local_player.realmName].debugMode = locals.NuNDebug;
	end
	if ( NuNSettings[local_player.realmName].processChat ) then
		locals.processAddMessage = NuNSettings[local_player.realmName].processChat;
	else
		NuNSettings[local_player.realmName].processChat = locals.processAddMessage;
	end

	-- 5.60 Database shortcuts
	locals.NuNDataPlayers = NuNData[local_player.realmName];
--evo NuN_Message("NuNF.NuN_InitialiseSavedVariables   local_player.realmName (cVar:realmName): '" .. tostring(local_player.realmName) .. ")" .. "  data:" .. tostring(locals.NuNDataPlayers));
	NuNDataANotes = NuNData[locals.Notes_dbKey];
	NuNDataRNotes = NuNData[local_player.realmName][locals.Notes_dbKey];
	locals.questHistory.Tag = locals.player_Name;
	locals.questHistory.Realm = local_player.realmName;
	locals.questHistory.Index = 1;
	locals.questHistory.Title = locals.player_Name;
	NuNQuestHistory = NuNData[locals.questHistory.Realm].QuestHistory[locals.questHistory.Tag];
end

-- ele1 & ele2 are the note names as specified in the browser frame, which are all prefixed with a 1 char code for the type of note
function NuNF.NuN_SortQuestHistory(ele1, ele2)
	ele1 = strsub(ele1, 2);
	ele2 = strsub(ele2, 2);
	if ( NuNQuestHistory[ele1].sortDate > NuNQuestHistory[ele2].sortDate ) then
		return true;
	end

	return false;
end

-- 5.60
function NuNF.NuN_SortAltArray(ele1, ele2)
	if ( ele1.sortKey > ele2.sortKey ) then
		return true;
	end
	return false;
end

function NuNF.NuN_SortTalentArray(elemA, elemB)
	
	local sortElemAFirst = false;
	if elemA.talentTier < elemB.talentTier then
		sortElemAFirst = true;
	elseif elemA.talentTier == elemB.talentTier then
		sortElemAFirst = (elemA.talentColumn < elemB.talentColumn);
	end
	return sortElemAFirst;
end

function NuNF.NuN_CleanQuestText(dirtyText)
	if ( dirtyText ) then
		local cleanText = strgsub(dirtyText, locals.player_Name, "$N");

		local class = UnitClass("player");
		cleanText = strgsub(cleanText, class, "$C");
		cleanText = strgsub(cleanText, strlower(class), "$C");

		return cleanText;
	end
end

function NuNF.NuN_CreateLevelUpNote(levelUpName, newLevel, hp, mana, str, agi, sta, int, spr)
	if ( ( levelUpName ) and ( levelUpName ~= "" ) ) then
		local tmp_c_note = local_player.currentNote.general;
		local tmp_g_text = general.text;

		local_player.currentNote.general = levelUpName;
		NuNDataRNotes[local_player.currentNote.general] = {};
		NuNDataRNotes[local_player.currentNote.general].type = 1;
		general.text = NUN_LVL_REACHED.. newLevel .." : ";
		general.text = general.text.."\n    "..NuNF.NuN_GetDateStamp();
		general.text = general.text.."\n    "..NuNF.NuN_GetLoc();
		general.text = general.text.."\n\n    "..NUN_HIT_POINTS.." : "..hp;
		general.text = general.text.."\n    "..NUN_MANA.." : "..mana;
		general.text = general.text.."\n    "..NUN_STRENGTH.." : "..str;
		general.text = general.text.."\n    "..NUN_AGILITY.." : "..agi;
		general.text = general.text.."\n    "..NUN_STAMINA.." : "..sta;
		general.text = general.text.."\n    "..NUN_INTELLECT.." : "..int;
		general.text = general.text.."\n    "..NUN_SPIRIT.." : "..spr;
		NuNF.NuN_SetGText("Realm");

		local_player.currentNote.general = tmp_c_note;
		general.text = tmp_g_text;
	end
end

function NuNF.NuN_LocaliseDateStamp(dateStamp)
	if ( ( GetLocale() ~= "enUS" ) and ( GetLocale() ~= "enGB" ) and ( NUN_DAY_LIST ) and ( NUN_MONTH_LIST ) ) then
		dateStamp = strgsub(dateStamp, "Monday", NUN_DAY_LIST[1]);
		dateStamp = strgsub(dateStamp, "Tuesday", NUN_DAY_LIST[2]);
		dateStamp = strgsub(dateStamp, "Wednesday", NUN_DAY_LIST[3]);
		dateStamp = strgsub(dateStamp, "Thursday", NUN_DAY_LIST[4]);
		dateStamp = strgsub(dateStamp, "Friday", NUN_DAY_LIST[5]);
		dateStamp = strgsub(dateStamp, "Saturday", NUN_DAY_LIST[6]);
		dateStamp = strgsub(dateStamp, "Sunday", NUN_DAY_LIST[7]);
		dateStamp = strgsub(dateStamp, "January", NUN_MONTH_LIST[1]);
		dateStamp = strgsub(dateStamp, "February", NUN_MONTH_LIST[2]);
		dateStamp = strgsub(dateStamp, "March", NUN_MONTH_LIST[3]);
		dateStamp = strgsub(dateStamp, "April", NUN_MONTH_LIST[4]);
		dateStamp = strgsub(dateStamp, "May", NUN_MONTH_LIST[5]);
		dateStamp = strgsub(dateStamp, "June", NUN_MONTH_LIST[6]);
		dateStamp = strgsub(dateStamp, "July", NUN_MONTH_LIST[7]);
		dateStamp = strgsub(dateStamp, "August", NUN_MONTH_LIST[8]);
		dateStamp = strgsub(dateStamp, "September", NUN_MONTH_LIST[9]);
		dateStamp = strgsub(dateStamp, "October", NUN_MONTH_LIST[10]);
		dateStamp = strgsub(dateStamp, "November", NUN_MONTH_LIST[11]);
		dateStamp = strgsub(dateStamp, "December", NUN_MONTH_LIST[12]);
	end

	return dateStamp;
end

function NuNF.NuN_QuestLogButtons()
if ron_disabled then	
	local parentButtonName = "QuestLogScrollFrameButton";
	local nunButtonName = "NuN_QuestNotesButton";
	-- ensure no infinite loop by stopping after we've reached 50
	for bIndex = 1, 50 do
		local NuNparentButton = _G[parentButtonName .. bIndex];
		if NuNparentButton then
			local newButton = _G[nunButtonName..bIndex];
			if newButton then
				if ( newButton:GetParent() ~= NuNparentButton ) then
					newButton:SetParent(NuNparentButton)
				end
			else
				newButton = CreateFrame("BUTTON", nunButtonName .. bIndex, NuNparentButton, "NuN_NoteButtonTemplate4");
				if ( newButton ) then
					newButton:SetID(bIndex);
					newButton:SetPoint("RIGHT", "$parent", "LEFT", 20, -1);
					newButton:Show();
					NuNC.NUN_QUESTLOG_BUTTONS = NuNC.NUN_QUESTLOG_BUTTONS + 1;
					NuNparentButton = _G[parentButtonName .. bIndex + 1];
				else
					NuN_Error("CreateQuestLogNoteButtons(): FAILED TO CREATE NuN BUTTON (name:" .. nunButtonName .. bIndex .. "  parentButtonName:" .. parentButtonName .. bIndex .. ")");
					return;			-- unexpected error really ... but no point trying to create more
				end
			end
		else
--			NuN_Message("CreateQuestLogNoteButtons(): couldn't find quest log button named %s.  Ending iteration...", parentButtonName .. bIndex);
			return;
		end
	end
end
end

function NuNF.NuN_DtoH(r, g, b)
	r = strformat("%.2X", (r * 255));
	g = strformat("%.2X", (g * 255));
	b = strformat("%.2X", (b * 255));

	return ("|c00" .. r .. g .. b);
end

function NuNF.NuN_HtoD(h)
	local start = 5;
	
	if ( strlen(h) == 6 ) then
		start = 1;
	end

	local r = strlower( strsub(h, start, start+1) );
	local g = strlower( strsub(h, start+2, start+3) );
	local b = strlower( strsub(h, start+4, start+5) );

	r = ( NuNC.hArray[ strsub(r, 1, 1) ] * 16 + NuNC.hArray[ strsub(r, 2, 2) ] ) / 255;
	g = ( NuNC.hArray[ strsub(g, 1, 1) ] * 16 + NuNC.hArray[ strsub(g, 2, 2) ] ) / 255;
	b = ( NuNC.hArray[ strsub(b, 1, 1) ] * 16 + NuNC.hArray[ strsub(b, 2, 2) ] ) / 255;

	return r, g, b;
end


-- return text that has been highlighted in the Edit Boxes using the mouse
function NuNF.NuN_GetSelectedText(eBox)
	local tLen, tS, tE;

	if ( ( eBox.cPosStart ) and ( eBox.cPosEnd ) ) then
		tLen = math.abs( eBox.cPosStart - eBox.cPosEnd );
	end

	if ( ( tLen ) and ( tLen > 0 ) ) then
		if ( eBox.cPosStart < eBox.cPosEnd ) then
			tS = eBox.cPosStart;
			tE = eBox.cPosEnd;
		else
			tS = eBox.cPosEnd;
			tE = eBox.cPosStart;
		end

		local selectedText = eBox:GetText();
		selectedText = strsub(selectedText, tS+1, tE);

		if ( selectedText ) then
			return selectedText;
		end
	end

	return nil;
end

function NuNF.CapitaliseName(parm1)
	local contactName, newInitial, remainder;

	local initial = strsub(parm1, 1, 1);
	local t2 = strsub(parm1, 2, 2);
	local remainder = strsub(parm1,2);
	remainder = strlower( remainder );

	local t1 = strbyte( strsub(initial, 1, 1) );
	if ( t1 == 195 ) then
		if ( t2 ) then
			t2 = strbyte(t2);

			if ( ( t2 ) and ( t2 > 159 ) and ( t2 < 190 ) ) then
				t2 = t2 - 32;
				newInitial = strchar(t1) .. strchar(t2);

			elseif ( ( t2 > 127 ) and ( t2 < 160 ) ) then
				newInitial = strchar(t1) .. strchar(t2);
			end
		end
	end

	if ( newInitial ) then
		contactName = newInitial..remainder;
		initial = newInitial;

	else
		initial = strupper(initial);
		if ( strlen(initial) > 0 ) then
			contactName = initial..remainder;

		else

			contactName = initial .. remainder;	-- I give up - they should pass capitalised...
			initial = nil;
			remainder = nil;
		end
	end

	return contactName, initial, remainder;
end


local sn = {};
	sn.Choice = nil;
	sn.Name = "";
	sn.Body = "";
	sn.CancelAll = nil;
	sn.totCount = 0;
	sn.Count = 0;
	sn.Imported, sn.Ignored, sn.Exists = 0, 0, 0;
	sn.Array = {};
	sn.Waiting = nil;
	
-- NotesUNeed Choose Note Importing type for Social Notes
StaticPopupDialogs["NUN_CHOOSE_SN_IMPORT"] = {
	text = TEXT(NUN_SN_IMPORT),
	button1 = TEXT(NUN_SN_IMPORT_CHOICE),
	button2 = TEXT(CANCEL),
	OnShow = function(self)
		sn.Waiting = true;
		sn.Choice = nil;
		_G[self:GetName().."Text"]:SetText( NUN_SN_IMPORT.." "..sn.Count.."/"..sn.totCount.." : "..sn.Name.." ?" );
	end,
	OnAccept = function()
		if ( ( IsControlKeyDown() ) and ( not IsAltKeyDown() ) ) then
			sn.Choice = "Alliance";
			return nil;
		elseif ( ( IsAltKeyDown() ) and ( not IsControlKeyDown() ) ) then
			sn.Choice = "Horde";
			return nil;
		else
			return true;
		end
	end,
	OnCancel = function()
		if ( IsShiftKeyDown() ) then
			sn.CancelAll = true;
		end
	end,
	OnHide = function()
		if ( sn.Choice ) then
			locals.NuNDataPlayers[sn.Name] = {};
			locals.NuNDataPlayers[sn.Name].faction = sn.Choice;
			local tmpTxt = contact.text;
			contact.text = sn.Body .. "\n" .. NUN_SN_FLAG;
			NuNF.NuN_SetCText(sn.Name);
			contact.text = tmpTxt;
			sn.Imported = sn.Imported + 1;
		else
			sn.Ignored = sn.Ignored + 1;
		end
		sn.Waiting = nil;
	end,
	timeout = 0,
	hideOnEscape = 1,
};

-- Duplicate Record
StaticPopupDialogs["NUN_DUPLICATE_RECORD"] = {
	text = TEXT(NUN_DUPLICATE),
	button1 = TEXT(NUN_REPLACE),
	button2 = TEXT(CANCEL),
	showAlert = 1,
	timeout = 0,
	OnAccept = function()
		if ( ( locals.NuN_Receiving.type == "Contact" ) and ( NuNFrame:IsVisible() ) ) then
			if ( IsAltKeyDown() ) then
				local oriText = NuNF.NuN_GetCText(local_player.currentNote.unit);
				local newText = NuNText:GetText();
				if ( not oriText ) then oriText = ""; end
				if ( not newText ) then newText = ""; end
				NuNText:SetText( oriText.."\n\n"..newText );
			end
			NuN_Delete(true);
			NuN_WriteNote();
		elseif ( NuNGNoteFrame:IsVisible() ) then
			if ( IsAltKeyDown() ) then
				local oriText = NuNF.NuN_GetGText(local_player.currentNote.general);
				local newText = NuNGNoteTextScroll:GetText();
				if ( not oriText ) then oriText = ""; end
				if ( not newText ) then newText = ""; end
				NuNGNoteTextScroll:SetText(oriText.."\n\n"..newText);
			end
			NuNGNote_Delete(true);
			NuNGNote_WriteNote();
		end
	end,
	OnHide = function()
		if ( ( locals.NuN_Receiving.type == "General" ) and ( NuNGNoteFrame:IsVisible() ) ) then
			NuNGNoteFrame:Hide();
		elseif ( ( locals.NuN_Receiving.type == "Contact" ) and ( NuNFrame:IsVisible() ) ) then
			HideNUNFrame();
		end
		locals.NuN_Receiving = {};
		receiptDeadline = defaultReceiptDeadline;
		NuN_uCount = 999;
		NuN_tCount = 999;
		receiptPending = nil;
	end,
	hideOnEscape = 1,
};










-- DropDownBox Initialisers and OnClick functions Localised in-line
-- Most of the colouring code is also localised in-line


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------
-- Mod Functions --
-----------------------
function NuN_OnLoad(self)
	local continentID, zoneID, continent, zone;
	local c_zones = {};
	local switch;

	-- Apparently no Taint issues with the following, but haven't seen any errors caused, and I DEFINITELY want to be able to completely
	-- intercept some messages BEFORE they reach the Player
	
	-- @fixme orgevo, 11/3/2010, the way to do this correctly has changed since Telic first wrote this code.  Investigate how other addons are doing this....
--	NuNHooks.NuNOri_ChatFrame_MessageEventHandler = ChatFrame_MessageEventHandler;
--	ChatFrame_MessageEventHandler = NuNNew_ChatFrame_MessageEventHandler;
--nun_msgf("NUN LOADING ITSELF - HOOKING CALENDAR FUNCTIONS FOR PROTECTION!!");
	-- if OpenCalendar is called too soon, it sort of pooches the calendar until the player logs back, for some reason.  So to prevent that, and so that we can
	-- load the GuildUI (which seems to call OpenCalendar at some point during its loading), temporarily set the global OpenCalendar function to point to an empty
	-- method.  We'll restore it later, once we're fully loaded and it's safe to call the function.
	NuNHooks.NuNOriginal_OpenCalendar = OpenCalendar;
	OpenCalendar = NuNOverrideOpenCalendar;

	self:RegisterEvent("WHO_LIST_UPDATE");			-- 5.60
	self:RegisterEvent("IGNORELIST_UPDATE");
	self:RegisterEvent("FRIENDLIST_UPDATE");
	self:RegisterEvent("PLAYER_ENTERING_WORLD");
	self:RegisterEvent("UPDATE_CHAT_WINDOWS");
	self:RegisterEvent("PLAYER_LEVEL_UP");
	self:RegisterEvent("QUEST_LOG_UPDATE");
	self:RegisterEvent("PARTY_MEMBERS_CHANGED");
	self:RegisterEvent("RAID_ROSTER_UPDATE");
	self:RegisterEvent("VARIABLES_LOADED");
	self:RegisterEvent("GUILD_ROSTER_UPDATE");		-- 5.60
	self:RegisterEvent("ADDON_LOADED");

	-- now we can load the GuildUI, so that we can hook the guild status update functions
	--@todo orgevo: I really don't like force-loading the GuildUI here, so early in the loading process.  It could be loaded later, but I'm doing something wrong
	-- with the guild notes buttons, when I'm trying to reparent them to the guild frame container buttons, so they don't show up.  gonna put this off till next
	-- release, however.
	locals.currentGuildRosterView = "playerStatus";
	if GuildFrame ~= nil then
		hooksecurefunc("GuildRoster_Update", NuNNew_GuildStatus_Update);
		hooksecurefunc("GuildRoster_UpdateTradeSkills", NuNNew_GuildStatus_Update);
		hooksecurefunc("GuildRosterButton_OnClick", NuNNew_GuildRosterButton_OnClick);

		if NuNHooks.NuNOriginal_GuildRoster_SetView == nil then
			NuNHooks.NuNOriginal_GuildRoster_SetView = GuildRoster_SetView;
			GuildRoster_SetView = NuN_InterceptGuildRoster_SetView;
			GuildRoster_SetView(GetCVar("guildRosterView"));
		end
	end
	hooksecurefunc("FriendsList_Update", NuNNew_FriendsList_Update);
	hooksecurefunc("IgnoreList_Update", NuNNew_IgnoreList_Update);
	hooksecurefunc("WhoList_Update", NuNNew_WhoList_Update);
	hooksecurefunc("QuestLog_Update", NuNNew_QuestLog_Update);
	hooksecurefunc("AbandonQuest", NuNNew_AbandonQuest);
	hooksecurefunc("QuestDetailAcceptButton_OnClick", NuNNew_QuestDetailAcceptButton_OnClick);
	hooksecurefunc("QuestRewardCompleteButton_OnClick", NuNNew_QuestRewardCompleteButton_OnClick);
	hooksecurefunc("FriendsFrameFriendButton_OnClick", NuNNew_FriendsFrameFriendButton_OnClick);
	hooksecurefunc("FriendsFrameIgnoreButton_OnClick", NuNNew_FriendsFrameIgnoreButton_OnClick);
	hooksecurefunc("FriendsFrameWhoButton_OnClick", NuNNew_FriendsFrameWhoButton_OnClick);
	hooksecurefunc("PaperDollItemSlotButton_OnModifiedClick", NuNNew_PaperDollItemSlotButton_OnModifiedClick);
	--	hooksecurefunc("ToggleWorldMap", NuNNew_ToggleWorldMap);		@todo orgevo: hmm, thinking about deprecating support for these two map addons (alpha map, and map notes)
	hooksecurefunc("ToggleFrame", NuNNew_ToggleWorldMap);
	hooksecurefunc("ContainerFrameItemButton_OnModifiedClick", NuNNew_ContainerFrameItemButton_OnModifiedClick);
	--	hooksecurefunc("QuestWatch_Update", NuN_QuestWatch_Update);		@todo orgevo: update and restore this code
	hooksecurefunc("SetAbandonQuest", NuNNew_SetAbandonQuest);
	hooksecurefunc("AddFriend", NuNNew_AddFriend);
	hooksecurefunc("RemoveFriend", NuNNew_RemoveFriend);
	hooksecurefunc("AddIgnore", NuNNew_AddIgnore);
	hooksecurefunc("DelIgnore", NuNNew_DelIgnore);
	hooksecurefunc("AddOrDelIgnore", NuNNew_AddOrDelIgnore);
	
	-- hook into the chat window hyperlink clicks
	for _, frameName in pairs(CHAT_FRAMES) do
		local frame = _G[frameName];
		if frame then
--			frame:HookScript("OnHyperlinkClick", NuN_ChatFrameOnHyperlinkShow);
		end
	end

--	hooksecurefunc("ChatFrame_OnHyperlinkShow", NuN_OnHyperlinkShow);
--	hooksecurefunc("SetItemRef", NuNNew_SetItemRef);
	-- ChatFrame_OnHyperlinkShow calls SetItemRef, but we need to intercept that call so that the user doesn't get an error if they click our chat tag
	-- without using a modifier key
	NuNHooks.NuNOriginal_OnHyperlinkShow = ChatFrame_OnHyperlinkShow;
	ChatFrame_OnHyperlinkShow = NuN_ChatFrameOnHyperlinkShow;
	
--	NuNHooks.NuNOriginal_GetColoredName = GetColoredName;
--	GetColoredName = NuN_GetColoredName;
	
--  Removed several redundant event checks -- 5.60
	SlashCmdList["NOTEUN"] = function(pList)
		local gap = strfind(pList, " ");
		local parm1;
		if ( gap ) then
			switch = strsub(pList, 1, (gap - 1));
			parm1 = strsub(pList, (gap + 1));
		else
			switch = pList;
			parm1 = nil;
		end
		NuN_CmdLine(switch, parm1, pList);
	end

	SLASH_NOTEUN1 = "/nun";
--[===[@debug@
	SlashCmdList["RL"] = function(msg)
		ConsoleExec("reloadui");
	end
	SLASH_RL1 = "/rl";
--@end-debug@]===]
	locals.player_Name = UnitName("player");

	NuNF.ClearButtonChanges();

	-- Prepopulate localised continent/zone names for Location Stamping NuNF.NuN_GetLoc()
	locals.continents[1] = {};
	locals.continents[2] = {};
	locals.continents[3] = {};
	locals.continents[4] = {};
	locals.continents[5] = {};
	locals.continents[6] = {};
	local continent_zones = {};
	for continentID, continent in ipairs{GetMapContinents()} do
		wipe(continent_zones);
		
		if not locals.continents[continentID] then
			locals.continents[continentID] = {};
		end
			
		locals.continents[continentID].name = continent;
		for zoneID, zone in ipairs{GetMapZones(continentID)} do
			continent_zones[zoneID] = zone;
		end
		locals.continents[continentID].zones = continent_zones;
	end
end
 
function NuN_CmdLine(option, parm1, pList)
	local idx;
	local value;
	local initial;
	local remainder;
	local contactName;
	local switch;

	-- Just toggle the Options frame if no parameters passed
	if ( ( option == "" ) or ( option == nil ) ) then
		NuN_Options();
	else
		switch = strlower(option);

		
		-- help echoes the slash command list
		if ( ( switch == "-h" ) or ( switch == "-help" ) or ( switch == "?" ) or ( switch == "-?" ) ) then
			DEFAULT_CHAT_FRAME:AddMessage(" ");
			idx = 0;
			value = "NUN_HELP_TEXT"..idx;
			while( NuNC[value] ) do
				DEFAULT_CHAT_FRAME:AddMessage(NuNC[value]);
				idx = idx + 1;
				value = "NUN_HELP_TEXT"..idx;
			end
			DEFAULT_CHAT_FRAME:AddMessage(" ");

		-- Guild Refresh -- 5.60
		elseif ( switch == "-gr" ) then												-- 5.60
			NuN_GuildRefreshCheckBox_OnClick();										-- 5.60

		elseif ( switch == "-grv" ) then											-- 5.60
			NuN_GRVerboseCheckBox_OnClick();										-- 5.60
			
		-- Toggles the NotesUNeed Help Tooltips & the main NuN Game Tooltip
		elseif ( switch == "-tt" ) then
			NuN_ToggleToolTips();

		-- Enable Right Click Menu functionality
		elseif ( switch == "-righton" ) then
			if ( NuNSettings[local_player.realmName].rightClickMenu == false ) then
				NuN_SetupRatings(true);
			end
			NuNSettings[local_player.realmName].rightClickMenu = true;
			NuN_Message( NUN_PRATING );

		-- Disable Right Click Menu functionality
		elseif ( switch == "-rightoff" ) then
			NuNSettings[local_player.realmName].rightClickMenu = false;
			ReloadUI();

		-- Create Alliance / Horde Contact Notes without validating Player exists
		elseif  ( ( switch == "-ca" ) or ( switch == "-ch" ) ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
				return;
			end
			contactName, initial, remainder = NuNF.CapitaliseName(parm1);
			if ( locals.NuNDataPlayers[contactName] ) then
				if ( NuNFrame:IsVisible() ) then
					NuNEditDetailsFrame:Hide();
					HideNUNFrame();
				end
				NuN_ShowSavedNote(contactName);
			else
				NuN_CreateContact(contactName, switch);
			end

		-- Purges the Exported Records file in \SavedVariables; Allowing replacement / appending with another record set.
		elseif ( switch == "-purgeexport" ) then
			NuN_PurgeExport();

		-- Open up the Note Browser Frame with all notes
		elseif ( switch == "-a" ) then
			if ( receiptPending ) then
				return;
			end
			NuN_DisplayAll();

		-- Toggle the 4 Microbuttons Panel
		elseif ( switch == "-micro" ) then
			NuN_ToggleMicroButtons();

		-- Open up a new un-named General Note
		--  OR Open up the General Note with the Passed parameter name
		--  OR Open up a new General Note with the Passed parameter name
		elseif ( switch == "-g" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
				return;
			end
			if ( ( parm1 ~= nil ) and ( parm1 ~= "") ) then
				local exists = NuN_GNoteExists(parm1);
				NuNGNoteFrame.fromQuest = nil;
				if ( exists ) then
					NuN_ShowSavedGNote();
				else
					local_player.currentNote.general = parm1;
					contact.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
					NuN_ShowTitledGNote("");
				end
			else
				NuN_ShowNewGNote();
			end

		-- Open up your own Player Note if Nothing Targetted
		--  OR Open up / Create a Contact Note if another Player Targetted (Horde OR Alliance)
		--  OR Open up / Create a General Note if a Mob / NPC Targetted
		elseif ( switch == "-t" ) then
			if ( receiptPending ) then
				return;
			end
			NuN_FromTarget(false);

		-- old legacy fix for conversion from German to English								???
		elseif ( ( switch == "->de" ) or ( switch == "->en" ) ) then
			NuN_LangPatch(switch);

		elseif ( switch == "-sendc") then
			NuN_ManualTransmit(nil, "Contact", parm1);
		
		elseif ( switch == "-sendg" ) then
			NuN_ManualTransmit(nil, "General", parm1);
		
		elseif ( switch == "-sendcf" ) then
			NuN_ManualTransmit(true, "Contact", parm1);

		elseif ( switch == "-sendgf" ) then
			NuN_ManualTransmit(true, "General", parm1);

		elseif ( switch == "-delay" ) then
			if ( parm1 ) then
				parm1 = tonumber(parm1);
			end
			if ( not parm1 ) then parm1 = 0.9; end
			local oldDelay = NuN_DTrans.tDelay;
			NuN_DTrans.tDelay = parm1;
			NuNSettings[local_player.realmName].delay = NuN_DTrans.tDelay;
			NuN_Message(oldDelay .. " ---> " .. NuN_DTrans.tDelay);

		elseif ( switch == "-restrictwho" ) then
			if ( not NuNSettings[local_player.realmName].restrictwho ) then
				NuNSettings[local_player.realmName].restrictwho = true;
				NuN_Message("-restrictwho : On");
			else
				NuNSettings[local_player.realmName].restrictwho = nil;
				NuN_Message("-restrictwho : Off");
			end

		elseif ( switch == "-alternativewho" ) then
			if ( not NuNSettings[local_player.realmName].alternativewho ) then
				NuNSettings[local_player.realmName].alternativewho = true;
				NuN_Message("-alternativewho : On");
			else
				NuNSettings[local_player.realmName].alternativewho = nil;
				NuN_Message("-alternativewho : Off");
			end

		elseif ( switch == "-importsn" ) then
			NuN_ImportSocialNotes_Start();

		-- Execute the Note as LUA Script
		elseif ( switch == "-x" ) then
			NuN_ExecuteNote(parm1);

		-- Expects parm1 to be a Player name - NuN will stop trying to ignore said player
		elseif ( switch == "-i" ) then
			NuN_ResetPlayerIgnoreStatus(parm1);

		-- Expects parm1 to be a Player name - NuN will stop trying to befriend said player
		elseif ( switch == "-f" ) then
			NuN_ResetPlayerFriendStatus(parm1);

		-- Resets Friend / Ignore lists to current Toons lists
		elseif ( switch == "-resetlists" ) then
			NuN_ResetFriendIgnoreLists();
		
		elseif ( switch == "-debug" ) then
			NotesUNeed.NuN_Statics.ToggleDebugMode();
			
		elseif ( switch == "-verbosedebug" ) then
			if not locals.NuNDebugVerbose then
				locals.NuNDebugVerbose = true;
				NuN_Message("Verbose debug logging now enabled.");
			else
				locals.NuNDebugVerbose = nil;
				NuN_Message("Verbose debug logging now disabled.");
			end	
			
		elseif ( switch == "-togglechathandler" ) then
			NotesUNeed.NuN_Statics.ToggleAddMessageHandler();

		-- Try to open up existing Player note with passed parameter name
		--  OR Try to open up existing General note with passed parameter name
		--  OR Create Contact note if passed parameter is name of current target / or a member of Party or Raid
		--  OR bring up the Browser window and search for passed parameter
		else
			if ( receiptPending ) then
				return;
			end
			contactName, initial, remainder = NuNF.CapitaliseName(switch);	-- pass 'switch' NOT 'parm1'
			if ( locals.NuNDataPlayers[contactName] ) then
				if ( NuNFrame:IsVisible() ) then
					NuNEditDetailsFrame:Hide();
					HideNUNFrame();
				end
				NuN_ShowSavedNote(contactName);
			elseif ( NuN_GNoteExists(pList) ) then
				NuNGNoteFrame.fromQuest = nil;
				NuN_ShowSavedGNote();
			else
				local theUnitID = NuNF.NuN_UnitInfo(true, contactName);
				if ( theUnitID ) then
					NuN_NewContact(theUnitID);
				else
					NuN_SearchForNote("Text", pList);
				end
			end
		end
	end
end

function NuN_TargetIsValidForNote()
	-- simple wrapper for determining whether it's valid to create a note based on the current target
	-- currently returns true unconditionally

	-- add more conditions here....
	return true;
end

-- basic wrapper for encapsulating logic to determine whether the system (i.e. this add-on [not the player])
-- should open notes.
function NuN_IsOpeningNoteAllowed()
	return m_ShowingNoteMutex == false;
end

-- NuN does not Target other Players, but will create different kinds of notes based on whether the current target is Self / Player / NPC(including Mobs)
function NuN_FromTarget(autoHide,ignoreTarget)
	if NuN_IsOpeningNoteAllowed() then
		if ignoreTarget == true then
			NuN_ShowNewGNote();
		else
	local theUnitID = "target";
	local npcText;

	-- NuN_CheckTarget determines whether the target is : 
	--	Self		"S"
	--	NPC		"N"
	--	Player	"F"	-- I guess I started out thinking "F"riend, but this includes Players of the opposite Faction
	local tstValue = NuN_CheckTarget();

	if ( receiptPending ) then
		return;
	end

	-- <Alt> Clicking on Microbutton re-opens the last opened Contact note
	if ( IsAltKeyDown() ) then
		locals.NuN_LastOpen.type = "Contact";
		NuN_ReOpen();
		return;
	end

	-- <Shift> Clicking on Microbutton allows Note Creation for Target without leaving the NotesUNeed Frame Open (e.g. background Noting of Target)
	if ( IsShiftKeyDown() ) then
		autoHide = true;
	end

	-- if we have targetted a Mob/NPC, then create a General Note
	if ( tstValue == "N" ) then
		NuNGNoteFrame.fromQuest = nil;
		-- If a Note already exists for the Target, then Show it if NOT auto-hiding
		if ( ( NuNDataRNotes[local_player.currentNote.general] ) or ( NuNDataANotes[local_player.currentNote.general] ) ) then
			if ( autoHide ~= true ) then
				NuN_ShowSavedGNote();
			end
		-- Else Gather Mob / NPC Info for the creation of a new note
		else
			NPCInfo_Proceed = nil;
			NuN_NPCInfo(NuN_NPCCreateNote, autoHide);
		end

	-- else we have targetted a player controlled toon of some kind
	else
		-- if the note already exists, and not auto-hiding, then show it
		if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
					if tstValue == "S" and not UnitExists("target") then
						-- what's happened here is NuN thinks we are targeting ourself, but the game doesn't think we have a valid target.
						-- naturally this means we have nothing targeted, but why doesn't the code handle this correctly? .hmmm.
						-- (oh, and by handle correctly, I mean why doesn't NuN_CheckTarget() return a different code for no
						-- target vs. targeting yourself.  It seems feasible that you'd never want it to create a note for you
						-- (or open previously saved note for your character) when nothing is targeted.
					end
			if ( autoHide ~= true ) then
				NuN_ShowSavedNote(local_player.currentNote.unit);
			end
		-- else create a new Player note for the target, and auto-save and hide it again if necessary.
		else
			NuN_NewContact(theUnitID);
			if ( autoHide == true ) then
				NuN_WriteNote();
						HideNUNFrame();
				NuN_Message(local_player.currentNote.unit..NUN_AUTONOTED);
			end
		end
	end
		end
	end
end

-- Eventually called when NPC Tooltip shows up for current Target
-- The call the NuN_NPCInfo() will now go in to the main routine and fetch info from the tooltip, after which we can hide it again
-- Player shouldn't notice the Tooltip flashing up - or at least I never did
function NuN_NPCCreateNote(autoHide)
	contact.type = NuNGet_CommandID(NUN_NOTETYPES, "NPC");
	local npcText = NuN_NPCInfo();
	GameTooltip:ClearLines();
	GameTooltip:Hide();
	NuN_ShowTitledGNote(npcText);
	if ( autoHide == true ) then
		NuNGNote_WriteNote();
		if ( not NuN_ConfirmFrame:IsVisible() ) then
			NuNGNoteFrame:Hide();
			NuN_Message(local_player.currentNote.general..NUN_AUTONOTED);
		end
	end
end


-- Return information about the current target
-- If nothing actually targetted, then return a reference to your own Player character
function NuN_CheckTarget(arg1)
	local result = "";
	
	if ( UnitExists("target") ) then																	-- 20200
		local chkName = UnitName("target");																-- 20200
		if ( ( UnitPlayerControlled("target") )  and ( UnitIsUnit("player", "target") ) ) then			-- 20200
			local_player.currentNote.unit = locals.player_Name;																				-- 20200
			result = "S";																					-- 20200
			
		elseif ( ( ( UnitPlayerControlled("target") )  and (not UnitIsUnit("player", "target") ) ) or ( UnitInParty("target") ) or ( UnitInRaid("target") ) )then
			local_player.currentNote.unit = chkName;
			result = "F";

		elseif ( not UnitPlayerControlled("target") ) then
			local_player.currentNote.general = chkName;
			result = "N";
		end

	else																								-- 20200
		local_player.currentNote.unit = locals.player_Name;
		result = "S";		-- evo: it seems like this shouldn't return the exact same thing as if we have a valid unit targeted.
	end
	return result;
end



-- Open up the Note Browser i.e. NuNSearchFrame  (but not necessarily with ALL notes)
--  If <Alt>-Left Click, then open up just General Notes (i.e. no Player Notes)
--  If Right Click, then open up Quest History (for current Toon)
function NuN_DisplayAll(mButton)
	if ( not mButton ) then
		mButton = "LeftButton";
	end

	if ( NuNSearchFrame:IsVisible() ) then
		NuNSearchFrame:Hide();
	else
		-- Commands are all in English; Localisation files associate localised variables with the English commands
		local sTyp = "All";	-- default
		if ( mButton == "LeftButton" ) then
			if ( IsAltKeyDown() ) then
				sTyp = "Notes";
			end
		else
			sTyp = "Quest History";
		end
		NuNSearchFrameBackButton:Disable();
		NuNSearchFrame.backEnabled = nil;
		locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, sTyp);
		locals.searchType = NUN_SEARCHFOR[locals.dropdownFrames.ddSearch].Command;
		NuNOptions_Search();							-- Actually Opens the Note Browser with the appropriate Note Filtering algorithm
	end
end



-- Toggle the Options Frame
function NuN_Options()
	if ( NuNOptionsFrame:IsVisible() ) then
		NuNOptionsFrame:Hide();
	else
		if ( ( MapNotes_OnLoad ) or ( MetaMap_Quicknote ) ) then
			NuN_AutoMapCheckBox:Enable();
			if ( NuNSettings[local_player.realmName].autoMapNotes ) then
				NuN_AutoMapCheckBox:SetChecked(1);
			else
				NuN_AutoMapCheckBox:SetChecked(0);
			end
		else
			NuN_AutoMapCheckBox:SetChecked(0);
			NuN_AutoMapCheckBoxLabel:SetText(NUN_NOMAPNOTES);
			NuN_AutoMapCheckBox:Disable();
		end
		if ( NuNFrame:IsVisible() ) then
			NuNEditDetailsFrame:Hide();
			HideNUNFrame();
		end
		if ( NuNSearchFrame:IsVisible() ) then
			NuNSearchFrame:Hide();
		end
		if ( NuNGNoteFrame:IsVisible() ) then
			NuNGNoteFrame:Hide();
		end
		UIDropDownMenu_SetSelectedID(NuNOptionsSearchDropDown, 1);
		UIDropDownMenu_SetText(NuNOptionsSearchDropDown, NUN_SEARCHFOR[1].Display);
		locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "All");
		if ( NuNSettings[local_player.realmName].autoG ) then
			NuNOptionsGuildCheckButton:SetChecked(1);
		else
			NuNOptionsGuildCheckButton:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].hignores ) then
			NuN_HignoresCheckBox:SetChecked(1);
		else
			NuN_HignoresCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoA ) then
			NuNOptionsAddCheckButton:SetChecked(1);
		else
			NuNOptionsAddCheckButton:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoFI ) then
			NuNOptionsAACheckButton:SetChecked(1);
		else
			NuNOptionsAACheckButton:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoS ) then
			NuNOptionsVerboseCheckButton:SetChecked(1);
		else
			NuNOptionsVerboseCheckButton:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoD ) then
			NuNOptionsDeleteCheckButton:SetChecked(1);
		else
			NuNOptionsDeleteCheckButton:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoQ ) then
			NuN_AutoQuestCheckBox:SetChecked(1);
		else
			NuN_AutoQuestCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoN ) then
			NuN_AutoNoteCheckBox:SetChecked(1);
		else
			NuN_AutoNoteCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].dLevel ) then
			NuN_DefaultLevelCheckBox:SetChecked(1);
		else
			NuN_DefaultLevelCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].toolTips ) then
			NuN_HelpTTCheckBox:SetChecked(1);
		else
			NuN_HelpTTCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoP ) then
			NuN_AutoPartyCheckBox:SetChecked(1);
		else
			NuN_AutoPartyCheckBox:SetChecked(0);
		end
		-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options
		if ( NuNSettings[local_player.realmName].autoGuildNotes ) then
			NuN_GuildRefreshCheckBox:SetChecked(1);
		else
			NuN_GuildRefreshCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].autoGRVerbose ) then
			NuN_GRVerboseCheckBox:SetChecked(1);
		else
			NuN_GRVerboseCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].nunFont ) then
			NuN_CustomFontCheckBox:SetChecked(1);
		else
			NuN_CustomFontCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].modifier == "on" ) then
			NuN_ModifierMasterCheckBox:SetChecked(1);
			NuNOptionsModifier:Enable();
			NuNOptions_SetModifierText();
		else
			NuN_ModifierMasterCheckBox:SetChecked(0);
			NuNOptionsModifier:SetText("n/a");
			NuNOptionsModifier:Disable();
		end
		-- 5.60 Options 	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options	-- 5.60 Options
		if ( NuNSettings[local_player.realmName].hideMicro ) then
			NuN_MicroCheckBox:SetChecked(0);
		else
			NuN_MicroCheckBox:SetChecked(1);
		end
		if ( NuNSettings[local_player.realmName].minOver ) then
			NuN_OverTTCheckBox:SetChecked(1);
		else
			NuN_OverTTCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].chatty ) then
			NuN_ChatTagCheckBox:SetChecked(1);
		else
			NuN_ChatTagCheckBox:SetChecked(0);
		end
		if ( NuNSettings[local_player.realmName].bHave ) then
			NuN_BehaveCheckBox:SetChecked(1);
		else
			NuN_BehaveCheckBox:SetChecked(0);
		end

		NuNOptionsTTLengthTextBox:SetText( NuNSettings[local_player.realmName].ttLen );
		NuNOptionsTTLineLengthTextBox:SetText( NuNSettings[local_player.realmName].ttLLen );
		NuNSearchFrameBackButton:Enable();
		NuNSearchFrame.backEnabled = true;
		NuNOptionsFrame:SetScale(NuNSettings[local_player.realmName].pScale);

		NuNOptionsFrame:Show();
	end
end



function NuN_ShowSavedNote(cName)
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	elseif ( not cName ) then
		return;
	end
	-- reset globals
	local_player.currentNote.unit = cName;
	contact.class = nil;
	contact.race = nil;
	contact.sex = nil;
	contact.prof1 = nil;
	contact.prof2 = nil;
	contact.arena = nil;
	contact.hrank = nil;
	contact.guild = nil;
	gRank = nil;
	gRankIndex = nil;
	gNote = nil;
	gOfficerNote = nil;
	contact.route = "Saved";
	if ( locals.NuNDataPlayers[local_player.currentNote.unit].faction == "Horde" ) then
		NuNF.NuN_HordeSetup();
	else
		NuNF.NuN_AllianceSetup();
	end
	NuN_ShowNote();
end



function NuN_ShowWhoNote(cName)
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end

	local_player.currentNote.unit = cName;
	contact.class = nil;
	contact.race = nil;
	contact.sex = nil;
	contact.prof1 = nil;
	contact.prof2 = nil;
	contact.arena = nil;
	contact.hrank = nil;
	contact.guild = nil;
	gRank = nil;
	gRankIndex = nil;
	gNote = nil;
	gOfficerNote = nil;
	contact.route = "Who";
	if ( NuN_horde ) then
		NuNF.NuN_HordeSetup();
	else
		NuNF.NuN_AllianceSetup();
	end
	NuN_ShowNote();
end

function NuN_ShowNote()
		-- indented further to indicate which lines are more housekeeping, and not really relevant to the function.
		if m_ShowingNoteMutex then
			-- we're being recursively called from this function....ignore.
			-- it might make sense to convert this to a semaphore at some point.
			return;
		end
		m_ShowingNoteMutex = true;

	local hText;
	local theText;
	
	-- Toggle the frame if for the same Player
	if ( ( NuNFrame:IsVisible() ) and ( locals.prevName == local_player.currentNote.unit ) ) then
		HideNUNFrame();
	else
		if NuNFrame:IsVisible() then
			HideNUNFrame();
		end
		locals.prevName = local_player.currentNote.unit;		-- save for toggle check

		if ( NuNOptionsFrame:IsVisible() ) then
			NuNOptionsFrame:Hide();
		end

		locals.lastDD = nil;					-- just opened so no changes to Drop Down Boxes yet
		NuNButtonClrDD:Disable();

		NuNF.ClearButtonChanges();			-- just opened so no changes to User Definable Buttons yet

		NuNHeader:SetText(local_player.currentNote.unit);

		-- if loading a saved note, then access SavedVariables
		if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
			contact.text = NuNF.NuN_GetCText(local_player.currentNote.unit);
			if ( ( contact.text == nil ) or ( contact.text == "" ) ) then
				contact.text = "\n";
			end
			NuNText:SetText( contact.text );
			if ( ( NuNSettings[local_player.realmName].autoA ) and ( ( locals.NuNDataPlayers[local_player.currentNote.unit].friendLst ) or ( locals.NuNDataPlayers[local_player.currentNote.unit].ignoreLst ) ) ) then
				NuNButtonDelete:Disable();
			else
				NuNButtonDelete:Enable();
			end
			NuNCOpenChatButton:Enable();
			NuNCTTCheckBoxLabel:Show();
			NuN_CTTCheckBox:Show();
			NuN_CTTCheckBox:SetChecked(0);
			if ( NuN_PinnedTooltip.type == "Contact" ) then
				NuN_CTTCheckBox:SetChecked( NuN_CheckPinnedBox(local_player.currentNote.unit) );
			end
			if ( local_player.currentNote.unit == locals.player_Name ) then
				NuNHeader:SetText(NUN_PLAYER.." : "..local_player.currentNote.unit);
			elseif ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_AUTO_C ) then
				NuNHeader:SetText(NUN_AUTO.." : "..local_player.currentNote.unit);
			elseif ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_MANU_C ) then
				NuNHeader:SetText(NUN_MANU.." : "..local_player.currentNote.unit);
			elseif ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_PARTY_C ) then
				NuNHeader:SetText(NuN_Strings.NUN_PARTY.." : "..local_player.currentNote.unit );
			elseif ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_SELF_C ) then
				NuNHeader:SetText(NUN_SELF.." : "..local_player.currentNote.unit);
			elseif ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_GUILD_C ) then
				NuNHeader:SetText(NuN_Strings.NUN_GUILD.." : "..local_player.currentNote.unit);
			else
				NuNHeader:SetText(local_player.currentNote.unit);
			end
		else
			theText = "";
			NuNHeader:SetText(NUN_NEW.." : "..local_player.currentNote.unit);
			if ( gNote ~= nil ) then
				theText = "\n"..gNote;
			end
			if ( gOfficerNote ~= nil ) then
				theText = theText.."\n"..gOfficerNote;
			end
			if ( theText == "" ) then
				theText = "\n";
			end
			NuNText:SetText(theText);
			NuNButtonDelete:Disable();
			NuNCOpenChatButton:Disable();
			NuNCTTCheckBoxLabel:Hide();
			NuN_CTTCheckBox:Hide();
		end

		NuNF.UserButtons_Initialise();
		NuNF.DropDowns_Initialise();

		-- variables that save changes to drop down boxes BEFORE the actual note is saved
		locals.dropdownFrames.ddRace = nil;
		locals.dropdownFrames.ddClass = nil;
		locals.dropdownFrames.ddSex = nil;
		locals.dropdownFrames.ddPRating = nil;
		locals.dropdownFrames.ddProf1 = nil;
		locals.dropdownFrames.ddProf2 = nil;
		locals.dropdownFrames.ddArena = nil;
		locals.dropdownFrames.ddHRank = nil;

		if ( NuNEditDetailsFrame:IsVisible() ) then
			NuNEditDetailsFrame:Hide();
		end
		if ( NuNcDeleteFrame:IsVisible() ) then
			NuNcDeleteFrame:Hide();
		end

		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partied ) ) then
--			NuNPartiedLabel:Show();
			NuNPartiedNumberLabel:SetText("(x"..tostring(locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partied)..")");
			NuNPartiedNumberLabel:Show();
			NuNFramePartyDownButton:Show();
		else
--			NuNPartiedLabel:Hide();
			NuNPartiedNumberLabel:SetText("(0)");
			NuNPartiedNumberLabel:Hide();
			NuNFramePartyDownButton:Hide();
		end
		NuNFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		NuNFrame:Show();

		-- Try to auto-populate more data about any opening Player Note												-- 5.60
		if ( not NuNSettings[local_player.realmName].restrictwho ) then
			NuN_Who();																			-- 5.60
		end

		-- One of the NuN Options controls whether the Text box gains focus automatically when you open a note
		if ( not NuNSettings[local_player.realmName].bHave ) then
			NuNText:SetFocus();
		else
			NuNText:ClearFocus();
		end
	end
	
		m_ShowingNoteMutex = false;
end


function NuN_HideIgnores(self)
--	NuN_Message("Wibble");
	if ( self:GetChecked() ) then
		NuNSettings[local_player.realmName].hignores = true;
	else
		NuNSettings[local_player.realmName].hignores = nil;
	end
end


-- automatic montoring of Ignore List for note creation and sharing with Alts
NuN_Update_Ignored = function()
	local idx;
	local value;
	local x;
	local iName;
	locals.NuN_FriendIgnoreActivity = true;
	local stopIgnoring = {};
	local startIgnoring = {};
	locals.NuN_IgnoreUpdate.func = nil;
	locals.NuN_IgnoreUpdate.name = nil;
	locals.NuN_IgnoreUpdate.time = 0;
	locals.NuN_FriendUpdate.func = nil;
	locals.NuN_FriendUpdate.name = nil;
	locals.NuN_FriendUpdate.time = 0;

	if not NuNSettings or not local_player.realmName or not NuNSettings[local_player.realmName] then
		-- true result indicates that the caller should try again later.
		return true;
	end
	if ( not NuNSettings[local_player.realmName].gNotFriends ) then
		NuNSettings[local_player.realmName].gNotFriends = {};
	end

	if ( local_player.factionName ~= nil ) then
		local isIgnored = {};
		for i = 1, GetNumIgnores(), 1 do
			local lName = GetIgnoreName(i);
			if ( ( not lName ) or ( lName == "" ) or ( lName == UNKNOWN ) or ( lName == UNKNOWNOBJECT ) ) then -- 5.60
				return true;
			else
				isIgnored[lName] = true;
			end
		end

		-- Check WoW Ignored List and validate against Saved Data
		for i = 1, GetNumIgnores(), 1 do
			iName = GetIgnoreName(i);
			if ( ( iName ) and ( iName ~= UNKNOWN ) and ( iName ~= UNKNOWNOBJECT ) ) then -- 5.60
				if ( NuNSettings[local_player.realmName].gNotIgnores[iName] )  then
					if ( NuNSettings[local_player.realmName].autoA ) then
						stopIgnoring[iName] = true;
					end
					if ( ( locals.NuNDataPlayers[iName] ) and ( locals.NuNDataPlayers[iName].ignoreLst ) ) then
						if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[iName].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[iName].friendLst ) ) then
							locals.NuNDataPlayers[iName] = nil;
						else
							locals.NuNDataPlayers[iName].ignoreLst = nil;
						end
					end

				elseif ( ( NuNSettings[local_player.realmName].autoA ) and ( not locals.NuNDataPlayers[iName] ) ) then
					locals.NuNDataPlayers[iName] = {};
					locals.NuNDataPlayers[iName].type = NuNC.NUN_AUTO_C;
					locals.NuNDataPlayers[iName].faction = local_player.factionName;
					locals.NuNDataPlayers[iName][locals.txtTxt] = NUN_AUTO_IGNORE..NuNF.NuN_GetDateStamp();
					locals.NuNDataPlayers[iName].ignoreLst = true;
				end
			end
		end

		-- Check Saved Data and validate against WoW Ignored List
		for idx, value in pairs(locals.NuNDataPlayers) do
			if ( ( locals.NuNDataPlayers[idx].faction) and ( locals.NuNDataPlayers[idx].faction == local_player.factionName ) and ( idx ~= locals.player_Name ) ) then
				local isIndexIgnored = false;
				if ( isIgnored[idx] ) then
					isIndexIgnored = true;
				end

				if ( ( isIndexIgnored ) and ( NuNSettings[local_player.realmName].gNotIgnores[idx] ) ) then
					if ( ( NuNSettings[local_player.realmName].autoA ) and ( not stopIgnoring[idx] ) ) then
						stopIgnoring[idx] = true;
					end
					if ( ( locals.NuNDataPlayers[idx] ) and ( locals.NuNDataPlayers[idx].ignoreLst ) ) then
						if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[idx].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[idx].friendLst ) ) then
							locals.NuNDataPlayers[idx] = nil;
						else
							locals.NuNDataPlayers[idx].ignoreLst = nil;
						end
					end

				elseif ( isIndexIgnored ) then
					if ( not locals.NuNDataPlayers[idx].ignoreLst ) then			-- Ignored but no ignore list currently
						locals.NuNDataPlayers[idx].ignoreLst = true;
					end

				else
					if ( locals.NuNDataPlayers[idx].ignoreLst ) then
						if ( NuNSettings[local_player.realmName].gNotIgnores[idx] ) then
							if ( ( locals.NuNDataPlayers[idx].type == NuNC.NUN_AUTO_C ) and ( NuNSettings[local_player.realmName].autoD ) and ( not locals.NuNDataPlayers[idx].friendLst ) ) then
								locals.NuNDataPlayers[idx] = nil;
							else
								locals.NuNDataPlayers[idx].ignoreLst = nil;
							end

						else
							if ( NuNSettings[local_player.realmName].autoA ) then
								startIgnoring[idx] = true;
							end
							if ( locals.NuNDataPlayers[idx] ) then
								locals.NuNDataPlayers[idx].ignoreLst = true;
							elseif ( NuNSettings[local_player.realmName].autoFI ) then
								locals.NuNDataPlayers[idx] = {};
								locals.NuNDataPlayers[idx].type = NuNC.NUN_AUTO_C;
								locals.NuNDataPlayers[idx].faction = local_player.factionName;
								locals.NuNDataPlayers[idx][locals.txtTxt] = NUN_AUTO_IGNORE..NuNF.NuN_GetDateStamp();
								locals.NuNDataPlayers[idx].ignoreLst = true;
							end
						end
					end
				end
			end
		end
	end

	for _name, player in pairs(stopIgnoring) do
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			NuN_Message(DELETE.." "..IGNORE.." "..name);
		end
		locals.nameLastAttemptedIgnoreUpdate = _name;
		DelIgnore(_name);
	end

	for _name, player in pairs(startIgnoring) do
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			NuN_Message(IGNORE.." "..name);
		end
		locals.nameLastAttemptedIgnoreUpdate = _name;
		AddIgnore(_name);
	end

	locals.NuN_FriendIgnoreActivity = nil;

	return nil;
end


-- automatic monitoring of Friends list for note creation and sharing with alts
function NuN_Update_Friends()
	local idx;
	local value;
	local x;
	local iName;
	local addFriends = {};
	local remFriends = {};
	locals.NuN_FriendIgnoreActivity = true;
	locals.NuN_IgnoreUpdate.func = nil;
	locals.NuN_IgnoreUpdate.name = nil;
	locals.NuN_IgnoreUpdate.time = 0;
	locals.NuN_FriendUpdate.func = nil;
	locals.NuN_FriendUpdate.name = nil;
	locals.NuN_FriendUpdate.time = 0;

	if not NuNSettings or not local_player.realmName or not NuNSettings[local_player.realmName] then
		-- true result indicates that the caller should try again later.
		return true;
	end
	
	if ( not NuNSettings[local_player.realmName].gNotFriends ) then
		NuNSettings[local_player.realmName].gNotFriends = {};
	end

	if ( local_player.factionName ~= nil ) then
		local isFriendly = {};
		for i = 1, GetNumFriends(), 1 do
			local lName = GetFriendInfo(i);
			if ( ( not lName ) or ( lName == "" ) or ( lName == UNKNOWN ) or ( lName == UNKNOWNOBJECT ) ) then -- 5.60
				return true;
			else
				isFriendly[lName] = true;
			end
		end

		-- Check WoW Friend List and validate against Saved Data
		for i = 1, GetNumFriends(), 1 do
			iName = GetFriendInfo(i);
			if ( ( iName ~= nil ) and ( iName ~= UNKNOWN ) and ( iName ~= UNKNOWNOBJECT ) ) then -- 5.60
				if ( NuNSettings[local_player.realmName].gNotFriends[iName] )  then
					if ( NuNSettings[local_player.realmName].autoA ) then
						remFriends[iName] = true;
					end
					if ( ( locals.NuNDataPlayers[iName] ) and ( locals.NuNDataPlayers[iName].friendLst ) ) then
						if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[iName].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[iName].ignoreLst ) ) then
							locals.NuNDataPlayers[iName] = nil;
						else
							locals.NuNDataPlayers[iName].friendLst = nil;
						end
					end

				elseif ( ( not locals.NuNDataPlayers[iName] ) and ( NuNSettings[local_player.realmName].autoA ) ) then
					locals.NuNDataPlayers[iName] = {};
					locals.NuNDataPlayers[iName].type = NuNC.NUN_AUTO_C;
					locals.NuNDataPlayers[iName].faction = local_player.factionName;
					locals.NuNDataPlayers[iName][locals.txtTxt] = NUN_AUTO_FRIEND..NuNF.NuN_GetDateStamp();
					locals.NuNDataPlayers[iName].friendLst = true;
				end
			end
		end

		-- Check Saved Data and validate against WoW Friend List
		for idx, value in pairs(locals.NuNDataPlayers) do
			if  ( idx == locals.player_Name ) then
				locals.NuNDataPlayers[idx].type = NuNC.NUN_SELF_C;

			elseif ( ( locals.NuNDataPlayers[idx].faction ) and ( locals.NuNDataPlayers[idx].faction == local_player.factionName ) ) then
				local isIndexFriendly = false;
				if ( isFriendly[idx] ) then
					isIndexFriendly = true;
				end

				if ( ( isIndexFriendly ) and ( NuNSettings[local_player.realmName].gNotFriends[idx] ) ) then
					if ( ( NuNSettings[local_player.realmName].autoA ) and ( not remFriends[idx] ) ) then
						remFriends[idx] = true;
					end
					if ( ( locals.NuNDataPlayers[idx] ) and ( locals.NuNDataPlayers[idx].friendLst ) ) then
						if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[idx].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[idx].ignoreLst ) ) then
							locals.NuNDataPlayers[idx] = nil;
						else
							locals.NuNDataPlayers[idx].friendLst = nil;
						end
					end

				elseif ( isIndexFriendly ) then
					if ( not locals.NuNDataPlayers[idx].friendLst ) then			-- Ignored but no ignore list currently
						locals.NuNDataPlayers[idx].friendLst = true;
					end

				else
					if ( locals.NuNDataPlayers[idx].friendLst ) then
						if ( NuNSettings[local_player.realmName].gNotFriends[idx] ) then
							if ( ( locals.NuNDataPlayers[idx].type == NuNC.NUN_AUTO_C ) and ( NuNSettings[local_player.realmName].autoD ) and ( not locals.NuNDataPlayers[idx].ignoreLst ) ) then
								locals.NuNDataPlayers[idx] = nil;
							else
								locals.NuNDataPlayers[idx].friendLst = nil;
							end

						else
							if ( NuNSettings[local_player.realmName].autoA ) then
								addFriends[idx] = true;
							end
							if ( locals.NuNDataPlayers[idx] ) then
								locals.NuNDataPlayers[idx].friendLst = true;
							elseif ( NuNSettings[local_player.realmName].autoFI ) then
								locals.NuNDataPlayers[idx] = {};
								locals.NuNDataPlayers[idx].type = NuNC.NUN_AUTO_C;
								locals.NuNDataPlayers[idx].faction = local_player.factionName;
								locals.NuNDataPlayers[idx][locals.txtTxt] = NUN_AUTO_FRIEND..NuNF.NuN_GetDateStamp();
								locals.NuNDataPlayers[idx].friendLst = true;
							end
						end
					end
				end
			end
		end
	end

	for _name, player in pairs(addFriends) do
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			NuN_Message(FRIENDS.." "..name);
		end
		locals.nameLastAttemptedFriendUpdate = _name;
		AddFriend(_name);
	end

	for _name, player in pairs(remFriends) do
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			NuN_Message(DELETE.." "..FRIENDS.." "..name);
		end
		locals.nameLastAttemptedIgnoreUpdate = _name;
		RemoveFriend(_name);
	end

	locals.NuN_FriendIgnoreActivity = nil;

	return nil;
end



-- Little helper function
function NuNGet_CommandID(_tab, txt)
	for i = 1, getn(_tab), 1 do					-- #_tab
		if ( _tab[i].Command == txt ) then return i; end
	end
	return nil;
end



-- The actual Note Saving Routine for Contact Notes
-- (General notes have a different Save routine)
-- Note that Player notes are saved at Realm level, and we don't have to worry about duplicates
function NuN_WriteNote()
	if ( ( local_player.currentNote.unit == UNKNOWN ) or ( local_player.currentNote.unit == UNKNOWNOBJECT ) ) then -- 5.60
		return;
	end

	if (not locals.NuNDataPlayers[local_player.currentNote.unit]) then
		locals.NuNDataPlayers[local_player.currentNote.unit] = {};
	end

	if ( local_player.currentNote.unit == locals.player_Name ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].type = NuNC.NUN_SELF_C;
		NuNHeader:SetText(NUN_PLAYER.." : "..local_player.currentNote.unit);
	elseif ( ( not locals.NuNDataPlayers[local_player.currentNote.unit].type ) or ( locals.NuNDataPlayers[local_player.currentNote.unit].type == NuNC.NUN_AUTO_C ) ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].type = NuNC.NUN_MANU_C;
		NuNHeader:SetText(NUN_MANU.." : "..local_player.currentNote.unit);
	end

	if ( not locals.NuNDataPlayers[local_player.currentNote.unit].faction ) then
		if ( ( contact.route == "Target" ) or ( contact.route == "Create" ) or ( contact.route == "Receipt" ) ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].faction = c_faction;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].faction = local_player.factionName;
		end
	end

	-- update the note status in visible Blizzard frames to show note now exists
	if ( FriendsListFrame:IsVisible() ) then
		NuNNew_FriendsList_Update();
	elseif ( IgnoreListFrame:IsVisible() ) then
		NuNNew_IgnoreList_Update();
	elseif ( WhoFrame:IsVisible() ) then
		NuNNew_WhoList_Update();
	end
	if ( GuildFrame and GuildFrame:IsVisible() ) then
		NuNNew_GuildStatus_Update();
	end
	if ( contact.guild ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].guild = contact.guild;
	end
	if ( not locals.NuNDataPlayers[local_player.currentNote.unit].guild ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].guild = "";
	end

	if ( NuN_Is_Ignored(local_player.currentNote.unit) ) then
		if ( not locals.NuNDataPlayers[local_player.currentNote.unit].ignoreLst ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].ignoreLst = true;
		end
	end

	if ( NuN_Is_Friendly(local_player.currentNote.unit) ) then
		if ( not locals.NuNDataPlayers[local_player.currentNote.unit].friendLst ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].friendLst = true;
		end
	end

	-- if any of the Drop Down boxes have changed, then save the change in the note
	--  -1 is used to flag the value has been nulled
	if (locals.dropdownFrames.ddRace) then
		if ( locals.dropdownFrames.ddRace == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].race = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].race = locals.dropdownFrames.ddRace;
		end
		locals.dropdownFrames.ddRace = nil;
	elseif ( contact.race ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].race = NuNF.NuNGet_TableID(locals.Races, contact.race);
	end
	if (locals.dropdownFrames.ddClass) then
		if ( locals.dropdownFrames.ddClass == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].cls = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].cls = locals.dropdownFrames.ddClass;
		end
		locals.dropdownFrames.ddClass = nil;
	elseif ( contact.class ~= nil )  then
		locals.NuNDataPlayers[local_player.currentNote.unit].cls = NuNF.NuNGet_TableID(locals.Classes, contact.class);
	end
	if (locals.dropdownFrames.ddSex) then
		if ( locals.dropdownFrames.ddSex == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].sex = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].sex = locals.dropdownFrames.ddSex;
		end
		locals.dropdownFrames.ddSex = nil;
	elseif ( contact.sex ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].sex = NuNF.NuNGet_TableID(NUN_SEXES, contact.sex);
	end
	if ( locals.dropdownFrames.ddPRating ) then
		if ( locals.dropdownFrames.ddPRating == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].prating = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].prating = locals.dropdownFrames.ddPRating;
		end
	elseif ( contact.prating ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].prating = NuNF.NuNGet_TableID(NuNSettings.ratings, contact.prating);
	end
	if (locals.dropdownFrames.ddProf1) then
		if ( locals.dropdownFrames.ddProf1 == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].prof1 = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].prof1 = locals.dropdownFrames.ddProf1;
		end
		locals.dropdownFrames.ddProf1 = nil;
	elseif ( contact.prof1 ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].prof1 = NuNF.NuNGet_TableID(NUN_PROFESSIONS, contact.prof1);
	end
	if (locals.dropdownFrames.ddProf2) then
		if ( locals.dropdownFrames.ddProf2 == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].prof2 = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].prof2 = locals.dropdownFrames.ddProf2;
		end
		locals.dropdownFrames.ddProf2 = nil;
	elseif ( contact.prof2 ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].prof2 = NuNF.NuNGet_TableID(NUN_PROFESSIONS, contact.prof2);
	end
	if ( locals.dropdownFrames.ddArena ) then
		if ( locals.dropdownFrames.ddArena == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].arena = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].arena = locals.dropdownFrames.ddArena;
		end
	elseif ( contact.arena ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].arena = NuNF.NuNGet_TableID(NUN_ARENAR, contact.arena);
	end
	if (locals.dropdownFrames.ddHRank) then
		if ( locals.dropdownFrames.ddHRank == -1 ) then
			locals.NuNDataPlayers[local_player.currentNote.unit].hrank = nil;
		else
			locals.NuNDataPlayers[local_player.currentNote.unit].hrank = locals.dropdownFrames.ddHRank;
		end
		locals.dropdownFrames.ddHRank = nil;
	elseif ( contact.hrank ~= nil ) then
		locals.NuNDataPlayers[local_player.currentNote.unit].hrank = NuNF.NuNGet_TableID(locals.Ranks, contact.hrank);
	end

	contact.text = NuNText:GetText();
	NuNF.NuN_SetCText(local_player.currentNote.unit);				-- split the text in to chunks for saving, and carry out special character substitution
	contact.text = strgsub(contact.text, "\124\124", "|");
	contact.text = strgsub(contact.text, "|C", "|c");
	contact.text = strgsub(contact.text, "|R", "|r");
	contact.text = strgsub(contact.text, "||c", "|c");
	contact.text = strgsub(contact.text, "||r", "|r");
	NuNText:SetText(contact.text);
	NuN_AllowColours("Contact");

	-- if any User Definable buttons changed value, then save those changes
	local b;
	for n = 1, locals.uBttns, 1 do
		if (locals.bttnChanges[n] ~= "") and (locals.bttnChanges[n] ~= nil) then
			locals.headingNumber = locals.pHead..n;
			locals.headingName = local_player.currentNote.unit..locals.headingNumber;
			if (not locals.NuNDataPlayers[locals.headingName]) then
				locals.NuNDataPlayers[locals.headingName] = {};
			end
			if (locals.bttnChanges[n] == -1) then		-- 5.60 Use -1 to flag blank
				locals.NuNDataPlayers[locals.headingName].txt = "";					-- nil = default; "" = manually blanked. For User Button Headers.
			else
				locals.NuNDataPlayers[locals.headingName].txt = locals.bttnChanges[n];
			end
		end
		-- 5.60 code moved in to this loop rather than looping a second time below
		b = n + locals.detlOffset;
		if (locals.bttnChanges[b] ~= nil) and (locals.bttnChanges[b] ~= "") then	-- 5.60 test swap
			locals.headingDate = local_player.currentNote.unit.. locals.pDetl ..n;
			if (not locals.NuNDataPlayers[locals.headingDate]) then
				locals.NuNDataPlayers[locals.headingDate] = {};
			end
			if (locals.bttnChanges[b] == -1) then	-- 5.60 Use -1 to flag blank
				locals.NuNDataPlayers[locals.headingDate].txt = nil;
			else
				locals.NuNDataPlayers[locals.headingDate].txt = locals.bttnChanges[b];
			end
		end
	end

	-- Talents ?
	if ( ( NuNTalents.player ) and ( NuNTalents.total ) and ( NuNTalents.player == local_player.currentNote.unit ) and ( NuNTalents.total > 0 ) ) then
		-- copy NuNTalents details to .talents array
		locals.NuNDataPlayers[local_player.currentNote.unit].talents = {};
		NuNF.NuN_CopyTable(NuNTalents, locals.NuNDataPlayers[local_player.currentNote.unit].talents)
	end

	-- Refresh the note browser, as this write may need to be reflected in it
	if ( ( NuNSearchFrame:IsVisible() ) and ( not strfind(NuNSearchTitleText:GetText(), NUN_QUESTS_TEXT) ) ) then
		NuNSearch_Search();
	end

	NuNF.ClearButtonChanges();
	if ( ( NuNSettings[local_player.realmName].autoA ) and ( ( locals.NuNDataPlayers[local_player.currentNote.unit].friendLst ) or ( locals.NuNDataPlayers[local_player.currentNote.unit].ignoreLst ) ) ) then
	else
		NuNButtonDelete:Enable();
	end
	NuNCOpenChatButton:Enable();
	NuNCTTCheckBoxLabel:Show();
	NuN_CTTCheckBox:Show();

	-- Update the Pin up Tooltip if necessary
	if ( NuN_CTTCheckBox:GetChecked() ) then
		locals.ttName = local_player.currentNote.unit;
		NuN_PinnedTooltip:ClearLines();
		NuN_State.NuN_PinUpHeader = true;
		NuN_PinnedTooltip.type = "Contact";
		NuNSettings[local_player.realmName].pT = {};
		NuNSettings[local_player.realmName].pT.type = "Contact";
		NuNSettings[local_player.realmName].pT.name = locals.ttName;
		NuNF.NuN_BuildTT(NuN_PinnedTooltip);
		NuN_State.NuN_PinUpHeader = false;
		NuN_PinnedTooltip:Show();
		if ( NuN_State.pinnedTTMoved ) then
			NuN_PinnedTooltip:ClearAllPoints();
			NuN_PinnedTooltip:SetPoint("CENTER", "UIParent", "BOTTOMLEFT", NuN_PinnedTooltip.x, NuN_PinnedTooltip.y);
		end
	end

	if ( NuN_SaveReport ) then
		NuN_Message(NuNC.NUN_SAVED_NOTE .. " : " .. local_player.currentNote.unit);
	end

	-- MapNotes related functionality for linking NuN notes to MapNotes
	NuN_IndexAll();
end


-- The Note Saving function for General notes
function NuNGNote_WriteNote(noteName)
	local conflict = false;
	local value, idx, pad;
	local conflicts = 0;
	local saveLvl;
	local NuN_Creating = nil;

	-- need to fetch a reliable note name for this note; Can be passed as parm, or must be fetched from Frame components
	if ( ( noteName ) and ( type(noteName) == "string" ) and ( noteName ~= "" ) ) then
		local_player.currentNote.general = noteName;
	else
		if ( ( NuNGNoteTitleButton:IsVisible() ) and ( NuNGNoteTitleButtonText:GetText() ~= "" ) ) then
			local_player.currentNote.general = NuNGNoteTitleButtonText:GetText();
		else
			local_player.currentNote.general = NuNGNoteTextBox:GetText();
		end
	end

	-- Check that IF Saving this Note at Account Level, you won't wipe out Notes saved at Realm Level on other Realms
	-- i.e.	you can have multiple notes with the same name saved on different Realms, or ONE Account level note with that name
	--	you can NOT have an Account level note with the same name as Realm level notes (even on other Realms)
	NuNConflictedRealmsLabel:SetText(" ");
	if ( ( NuN_GLevel_CheckBox:GetChecked() ) and ( not NuNGNoteFrame.confirmed ) ) then
		for idx, value in pairs(NuNData) do
			if ( ( idx ~= local_player.realmName ) and ( NuNData[idx][locals.Notes_dbKey] ) ) then
				if ( NuNData[idx][locals.Notes_dbKey][local_player.currentNote.general] ) then
					conflicts = conflicts + 1;
					if ( conflicts == 1 ) then
						pad = "";
					else
						pad = ", ";
					end
					if ( conflicts < 5 ) then
						NuNConflictedRealmsLabel:SetText( NuNConflictedRealmsLabel:GetText()..pad..idx );
					end
					conflict = true;
				end
			end
		end
	end

	if ( ( conflict ) and ( not NuNGNoteFrame.confirmed ) ) then
		if ( conflicts > 4 ) then
			NuNConflictedRealmsLabel:SetText( NuNConflictedRealmsLabel:GetText().."...." );
		end
		NuN_ConfirmFrame:Show();
	else
		if ( NuN_ConfirmFrame:IsVisible() ) then
			NuN_ConfirmFrame:Hide();
		end
		if ( not locals.NuN_GNote_OriTitle ) then
			NuN_Creating = true;
			if ( ( locals.NuN_Receiving.active ) and ( local_player.currentNote.general == locals.NuN_Receiving.title ) ) then
				NuN_Creating = nil;
			end
		end

		if ( ( ( locals.NuN_GNote_OriTitle ) and ( locals.NuN_GNote_OriTitle ~= local_player.currentNote.general ) ) or ( not locals.NuN_GNote_OriTitle ) ) then
			if ( ( NuNDataRNotes[local_player.currentNote.general] ) or ( NuNDataANotes[local_player.currentNote.general] ) ) then
				NuN_SearchForNote("Text", local_player.currentNote.general);
				receiptPending = true;
				StaticPopup_Show("NUN_DUPLICATE_RECORD");
				return;
			else
				if ( NuNDataRNotes[locals.NuN_GNote_OriTitle] ) then
					NuNDataRNotes[locals.NuN_GNote_OriTitle] = nil;
				elseif ( NuNDataANotes[locals.NuN_GNote_OriTitle] ) then
					NuNDataANotes[locals.NuN_GNote_OriTitle] = nil;
				end
			end
		end

		locals.NuN_GNote_OriTitle = local_player.currentNote.general;
		general.text = NuNGNoteTextScroll:GetText();
		if ( general.text == nil ) then
			general.text = "";
		end

		if ( NuN_GLevel_CheckBox:GetChecked() ) then
			saveLvl = "Account";
			NuNDataANotes[local_player.currentNote.general] = {};
			for idx, value in pairs(NuNData) do
				if ( NuNData[idx][locals.Notes_dbKey] ) then
					if ( NuNData[idx][locals.Notes_dbKey][local_player.currentNote.general] ) then
						NuNData[idx][locals.Notes_dbKey][local_player.currentNote.general] = nil;
					end
				end
			end
		else
			saveLvl = "Realm";
			NuNDataRNotes[local_player.currentNote.general] = {};
			if ( NuNDataANotes[local_player.currentNote.general] ) then
				NuNDataANotes[local_player.currentNote.general] = nil;
			end
		end

		general.text = strgsub(general.text, "\124\124", "|");
		general.text = strgsub(general.text, "|C", "|c");
		general.text = strgsub(general.text, "|R", "|r");
		general.text = strgsub(general.text, "||c", "|c");
		general.text = strgsub(general.text, "||r", "|r");
		-- THIS is basically the moment of saving
		NuNF.NuN_SetGText(saveLvl);
		NuNGNoteTextScroll:SetText(general.text);
		NuN_AllowColours("General");

		-- Index Item Links against Simple text names, so that Item Link note names can be looked up from Simple text names
		if ( strfind(local_player.currentNote.general, "|Hitem:") ) then
			simpleName = NuNF.NuN_GetSimpleName(local_player.currentNote.general);
			if ( simpleName ~= nil ) then
				NuNData[locals.itmIndex_dbKey][simpleName] = local_player.currentNote.general;
			end
		end

		if ( not NuNGNoteFrame.type ) then
			NuNGNoteFrame.type = contact.type;
		end
		if ( not NuNGNoteFrame.type ) then
			NuNGNoteFrame.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
			contact.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
		end
		if ( NuNGNoteFrame.type ) then
			if ( NuN_GLevel_CheckBox:GetChecked() ) then
				NuNDataANotes[local_player.currentNote.general].type = NuNGNoteFrame.type;
			else
				NuNDataRNotes[local_player.currentNote.general].type = NuNGNoteFrame.type;
			end
			-- If a Quest Note, and auto-noting Quests, and no Quest History for this quest, then.........
			if ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "QST"  ) then
				if ( not NuNData[local_player.realmName].QuestHistory[locals.player_Name][local_player.currentNote.general] ) then
					NuNData[local_player.realmName].QuestHistory[locals.player_Name][local_player.currentNote.general] = {};
					NuNData[local_player.realmName].QuestHistory[locals.player_Name][local_player.currentNote.general].sortDate = tostring(date("%Y%m%d%H%M%S"));
					NuNData[local_player.realmName].QuestHistory[locals.player_Name][local_player.currentNote.general].pLevel = UnitLevel("player");
					local qTxt = NuNC.NUN_CREATED.."\n    "..NuNF.NuN_GetDateStamp().."\n    "..NuNF.NuN_GetLoc().."\n";
					NuNData[local_player.realmName].QuestHistory[locals.player_Name][local_player.currentNote.general].txt = NuNF.NuN_SetSaveText(qTxt);
					NuNF.NuN_UpdateQuestNotes("Write");
				end
				
			-- else if an NPC note, and auto-noting NPCs, and MapNotes is installed, then .............
			elseif ( ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "NPC" ) and ( NuN_Creating ) and ( NuNSettings[local_player.realmName].autoMapNotes ) ) then
				NuN_MapNote("Target", "", "", nil);
			end
		end

		-- Refresh other frames that might reflect the recent Save
		
		if ( QuestLogFrame:IsVisible() ) then
			QuestLog_Update();
		end

		if ( ( NuNSearchFrame:IsVisible() ) and ( NuNGNoteFrame.fromQuest ) ) then
			NuN_FetchQuestHistory();
		elseif ( ( NuNSearchFrame:IsVisible() ) and ( not strfind(NuNSearchTitleText:GetText(), NUN_QUESTS_TEXT) ) ) then
			NuNSearch_Search();
		end

		-- Update Note Buttons to reflect the fact it has been saved e.g. can now delete
		
		NuNGNoteButtonDelete:Enable();
		if ( ( MapNotes_OnLoad ) or ( MetaMap_Quicknote ) ) then
			NuNMapNoteButton:Enable();
		end
		NuNGOpenChatButton:Enable();
		NuN_GTTCheckBox:Show();
		NuN_GTTCheckBox:SetChecked(0);
		if ( NuN_PinnedTooltip.type == "General" ) then
			NuN_GTTCheckBox:SetChecked( NuN_CheckPinnedBox(local_player.currentNote.general) );
		end
		NuNGTTCheckBoxLabel:Show();
		NuNGNoteTitleButtonText:SetText(local_player.currentNote.general);
		NuNGNoteTextBox:Hide();
		NuNGNoteTitleButton:Show();
		NuNGNoteHeader:SetText(NuNC.NUN_SAVED_NOTE);

		if ( NuN_SaveReport ) then
			NuN_Message(NuNC.NUN_SAVED_NOTE .. " : " .. local_player.currentNote.general);
		end

		-- Update the Pinned up tooltip if necessary
		if ( NuN_GTTCheckBox:GetChecked() ) then
			locals.ttName = local_player.currentNote.general;
			NuN_PinnedTooltip:ClearLines();
			NuN_State.NuN_PinUpHeader = true;
			NuN_PinnedTooltip.type = "General";
			NuNSettings[local_player.realmName].pT = {};
			NuNSettings[local_player.realmName].pT.type = "General";
			NuNSettings[local_player.realmName].pT.name = locals.ttName;
			NuNF.NuN_BuildTT(NuN_PinnedTooltip);
			NuN_State.NuN_PinUpHeader = false;
			NuN_PinnedTooltip:Show();
			if ( NuN_State.pinnedTTMoved ) then
				NuN_PinnedTooltip:ClearAllPoints();
				NuN_PinnedTooltip:SetPoint("CENTER", "UIParent", "BOTTOMLEFT", NuN_PinnedTooltip.x, NuN_PinnedTooltip.y);
			end
		end
		NuN_IndexAll();
	end
end

function NuNNew_BLRemovePlayer(__, player, ...)
	local _name;
	if (player == "target") then
		_name = UnitName("target");
	else
		_name = player;
	end

	if (_name == nil) then
		idx = BlackList:GetSelectedBlackList();
	else
		idx = BlackList:GetIndexByName(_name);
	end

	if (idx > 0) then
		entry = BlackList:GetPlayerByIndex(idx);

		if ( entry ) then
			_name = entry.name;
			local NuNned = strfind(entry.reason, "[NuN]");
			if ( ( _name ) and ( NuNned ) and ( locals.NuNDataPlayers[_name] ) and ( locals.NuNDataPlayers[_name].prating ) ) then
				locals.NuNDataPlayers[_name].prating = nil;
			end		
		end
	end

	NuNHooks.NuNOri_BLRemovePlayer(__, player, ...);
end

function NuNOverrideOpenCalendar()
	nun_msgf("SOMEONE REQUESTED THE CALENDAR BE OPENED TOO SOON!!!");
end

function NuN_OnEvent(self,event,...)
	local arg1, arg2, arg3, arg4 = ...;
	if ( event == "VARIABLES_LOADED" ) then
		NuN_InitializeUpvalues();
		
		NuN_Message("NotesUNeed |c0000FF00"..NUN_VERSION.."|r "..NUN_LOADED);

		-- If Loading due to a ReloadUI triggered by NuN BackUp OR Restore, then finish processing
		if ( NuNSettings.BackedUp ) then
			NuNSettings.BackedUp = nil;
			NuN_Message( NUN_FINISHED_PROCESSING.." : "..NUN_OPT_BACKUP );
		elseif ( NuNSettings.Restored ) then
			NuNSettings.Restored = nil;
			NuN_Message( NUN_FINISHED_PROCESSING.." : "..NUN_OPT_RESTORE );
		end

		if ( TipBuddyTooltip ) then
			NuN_TipBuddyTooltipControl:SetParent(TipBuddyTooltip);
		end
		NuNHooks.NuNOri_AlphaMapNotes_OnEnter = AlphaMapNotes_OnEnter;
		AlphaMapNotes_OnEnter = NuNNew_AlphaMapNotes_OnEnter;

		locals.player_Name = UnitName("player");
		NuN_getFaction();
		
		NuNF.NuN_InitialiseSavedVariables();

		if ( not locals.NuNDataPlayers[locals.player_Name] ) then
			NuN_AutoNote();						-- function itself checks if option is checked
		end
		if ( not NuNSettings[local_player.realmName].hideMicro ) then
			NuNMicroFrame:Show();
		else
			NuNMicroFrame:Hide();
		end

		NuN_PinnedTooltip:SetScale(NuNSettings[local_player.realmName].tScale);
		NuN_Tooltip:SetScale(NuNSettings[local_player.realmName].tScale);
		WorldMapTooltip:SetScale(NuNSettings[local_player.realmName].mScale);
		NuN_MapTooltip:SetScale(NuNSettings[local_player.realmName].mScale);
		NuNPopup:SetScale(NuNSettings[local_player.realmName].mScale);

		if ( MapNotes_OnLoad ) then
			NuN_MapIndexHouseKeeping();
		end

		NuN_State.NuN_AtStartup = true;

		-- one time MapNotes function hooking
		if ( NuN_State.NuN_FirstTime ) then
			NuN_State.NuN_FirstTime = nil;
			if ( MapNotes_OnLoad ) then
				NuNHooks.NuNOri_MapNotes_OnEnter = MapNotes_OnEnter;
				MapNotes_OnEnter = NuNNew_MapNotes_OnEnter;
				NuNHooks.NuNOri_MapNotes_OnLeave = MapNotes_OnLeave;
				MapNotes_OnLeave = NuNNew_MapNotes_OnLeave;
--				NuNHooks.NuNOri_MapNotes_DeleteNote = MapNotes_DeleteNote;
--				MapNotes_DeleteNote = NuNNew_MapNotes_DeleteNote;
				NuNHooks.NuNOri_MapNotes_WriteNote = MapNotes_WriteNote;
				MapNotes_WriteNote = NuNNew_MapNotes_WriteNote;
				NuNHooks.NuNOri_MapNotes_Quicknote = MapNotes_Quicknote;
				MapNotes_Quicknote = NuNNew_MapNotes_Quicknote;
			end
		end

		-- If there is data available to Import, then enable the Import button
		for idx, data in pairs(NuNDataExport) do
			NuN_ImportModule = idx;
			NuN_ImportData = data;
		end
		if ( NuN_ImportModule ) then
			NuNOptionsDBImport:Enable();
		end

		-- alternative mono-spaced font for use in Note edit boxes
		if ( NuNSettings[local_player.realmName].nunFont ) then
			NuN_UpdateFont(NuNC.NUN_FONT1, 12);
			NuN_CustomFontCheckBox:SetChecked(1);
		end

		-- MapNotes related functions linking NuN to Map Notes
		NuN_IndexAll();

		-- Unit Right Click Menu changes
		if ( NuNSettings[local_player.realmName].rightClickMenu == nil ) then
			NuNSettings[local_player.realmName].rightClickMenu = true;
		end
		if ( NuNSettings[local_player.realmName].rightClickMenu == true ) then
			NuN_SetupRatings(true);
		end

		-- Variables loaded - GUILD ROSTER UPDATE
		-- Ensure Guild Roster Update, if the guild UI is loaded already; otherwise, do this when that addon loads
		if ( NuNSettings[local_player.realmName].autoGuildNotes ) then
			if ( NuNSettings[local_player.realmName].autoGRVerbose ) then
				NuN_SyncGuildMemberNotes("Startup");
			else
				NuN_SyncGuildMemberNotes();
			end
			NuN_State.NuN_syncGuildMemberNotes = true;

			if ( locals.NuNDebug ) then
				nun_msgf("VARIABLES_LOADED  syncGuildNotes:%s", tostring(NuN_State.NuN_syncGuildMemberNotes));
			end
			GuildRoster();
		end

		--Cosmos integration
		if (EarthFeature_AddButton) then
			EarthFeature_AddButton(
				{
					id = NUN_OPTIONS_HEADER;
					name = NUN_OPTIONS_HEADER;
					subtext = "";
					tooltip = "";
					icon = "Interface\\Buttons\\UI-CheckBox-Check";
					callback = NuN_Options;
					test = nil;
				}
			);
		elseif (Cosmos_RegisterButton) then
			Cosmos_RegisterButton(
				NUN_OPTIONS_HEADER,
				NUN_OPTIONS_HEADER,
				"",
				"Interface\\Buttons\\UI-CheckBox-Check",
				NuN_Options
			);
		end

		UIDropDownMenu_SetWidth(NuNARaceDropDown, 71);
		UIDropDownMenu_SetWidth(NuNHRaceDropDown, 71);
		UIDropDownMenu_SetWidth(NuNAClassDropDown, 70);
		UIDropDownMenu_SetWidth(NuNHClassDropDown, 70);
		UIDropDownMenu_SetWidth(NuNSexDropDown, 70);
		UIDropDownMenu_SetWidth(NuNPRatingDropDown, 109);
		UIDropDownMenu_SetWidth(NuNProf1DropDown, 216);
		UIDropDownMenu_SetWidth(NuNProf2DropDown, 216);
		UIDropDownMenu_SetWidth(NuNArenaRDropDown, 141);
		UIDropDownMenu_SetWidth(NuNAHRankDropDown, 141);
		UIDropDownMenu_SetWidth(NuNHHRankDropDown, 141);
		UIDropDownMenu_SetWidth(NuNOptionsSearchDropDown, 165);
		UIDropDownMenu_SetWidth(NuNChatDropDown, 110);
		UIDropDownMenu_SetWidth(NuNChannelDropDown, 175);
		UIDropDownMenu_SetWidth(NuNSearchClassDropDown, 204);
		UIDropDownMenu_SetWidth(NuNSearchProfDropDown, 204);
		UIDropDownMenu_SetWidth(NuNSearchQHDropDown, 204);
		UIDropDownMenu_SetWidth(NuNGTypeDropDown, 70);

		-- 5.60 Build array of Noted Alts
		local altCounter = 1;
		local sortKey;
		local tAltArray = {};
				tAltArray[1] = {};	-- for other Alts on this Realm
				tAltArray[2] = {};	-- for other Alts on other Realms

		AltArray[altCounter] = {};
		AltArray[altCounter].name = locals.player_Name;
		AltArray[altCounter].realm = local_player.realmName;
		AltArray[altCounter].displayName = locals.player_Name;
		
		for altName in pairs(NuNData[local_player.realmName].QuestHistory) do
			if ( altName ~= locals.player_Name ) then
				tAltArray[1][altCounter] = {};
				tAltArray[1][altCounter].name = altName;
				tAltArray[1][altCounter].realm = local_player.realmName;
				tAltArray[1][altCounter].displayName = altName;
				tAltArray[1][altCounter].sortKey = altName;
				altCounter = altCounter + 1;
			end
		end
		tsort(tAltArray[1], NuNF.NuN_SortAltArray);

		altCounter = 1;
		for altRealm in pairs(NuNData) do
			if ( ( NuNData[altRealm].QuestHistory ) and ( altRealm ~= local_player.realmName ) ) then
				for altName in pairs(NuNData[altRealm].QuestHistory) do
					sortKey = altRealm..altName;
					tAltArray[2][altCounter] = {};
					tAltArray[2][altCounter].name = altName;
					tAltArray[2][altCounter].realm = altRealm;
					tAltArray[2][altCounter].displayName = altName .. "-" .. altRealm;
					tAltArray[2][altCounter].sortKey = sortKey;
					altCounter = altCounter + 1;
				end
			end
		end
		tsort(tAltArray[2], NuNF.NuN_SortAltArray);

		altCounter = 1;	-- start at 2; locals.player_Name at 1
		for i=1, getn(tAltArray[1]), 1 do
			altCounter = altCounter + 1;
			AltArray[altCounter] = {};
			AltArray[altCounter].name = tAltArray[1][i].name;
			AltArray[altCounter].realm = tAltArray[1][i].realm;
			AltArray[altCounter].displayName = tAltArray[1][i].displayName;
		end
		for i=1, getn(tAltArray[2]), 1 do
			altCounter = altCounter + 1;
			AltArray[altCounter] = {};
			AltArray[altCounter].name = tAltArray[2][i].name;
			AltArray[altCounter].realm = tAltArray[2][i].realm;
			AltArray[altCounter].displayName = tAltArray[2][i].displayName;
		end

		-- Now can populate the Drop Down box
		UIDropDownMenu_SetSelectedID(NuNSearchQHDropDown, 1);
		UIDropDownMenu_SetText(NuNSearchQHDropDown, locals.player_Name);

		-- Set up colour picker presets
		local cpKey, bttn, r, g, b;
		for i=1, 5, 1 do
			-- Contact note frame button
			cpKey = "cc"..i;
			bttn = _G["NuNCColourPreset"..i];
			bttn.preset = NuNSettings[local_player.realmName][cpKey];
			bttn.parentType = "Contact";
			bttn:SetID(i);
			r, g, b = NuNF.NuN_HtoD(bttn.preset);
			_G[bttn:GetName().."Texture"]:SetVertexColor(r, g, b);
			-- General note frame button
			cpKey = "gc"..i;
			bttn = _G["NuNGColourPreset"..i];
			bttn.preset = NuNSettings[local_player.realmName][cpKey];
			bttn.parentType = "General";
			bttn:SetID(i);
			r, g, b = NuNF.NuN_HtoD(bttn.preset);
			_G[bttn:GetName().."Texture"]:SetVertexColor(r, g, b);
		end

		if ( BlackList ) then
			NuNHooks.NuNOri_BLRemovePlayer = BlackList.RemovePlayer;
			BlackList.RemovePlayer = NuNNew_BLRemovePlayer;
		end

		if ( NuNSettings[local_player.realmName].pT ) then
			locals.ttName = NuNSettings[local_player.realmName].pT.name;
			NuN_PinnedTooltip:ClearLines();
			NuN_PinnedTooltip:SetOwner(UIParent, ANCHOR_NONE);
			NuN_State.NuN_PinUpHeader = true;
			NuN_PinnedTooltip.type = NuNSettings[local_player.realmName].pT.type;
			NuN_PinnedTooltip.noteName = locals.ttName;
			NuNF.NuN_BuildTT(NuN_PinnedTooltip);
			NuN_State.NuN_PinUpHeader = false;
			NuN_PinnedTooltip.x = NuNSettings[local_player.realmName].pT.x;
			NuN_PinnedTooltip.y = NuNSettings[local_player.realmName].pT.y;
			NuN_PinnedTooltip:Show();
			NuN_State.togglePinUp = true;
		end

		for key, value in pairs( NuNData[locals.itmIndex_dbKey] ) do
			NuNData[locals.itmIndex_dbKey][key] = strgsub(value, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\|", ":0:0:0:0:0:0:0|");
			NuNData[locals.itmIndex_dbKey][key] = strgsub(value, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\124", ":0:0:0:0:0:0:0\124");
		end
		
		-- finally, initialize the value of the last opened note
		NuN_LoadLastOpenedNote();

	elseif ( event == "ADDON_LOADED" ) then
		nun_msgf("ADDON_LOADED EVENT RECEIVED - addon:%s", tostring(arg1));
		if ( arg1 == "NotesUNeed" ) then
			-- restore the original OpenCalendar function, now that we're fully loaded.
			OpenCalendar = NuNHooks.NuNOriginal_OpenCalendar;
			NuNHooks.NuNOriginal_OpenCalendar = nil;
		elseif ( arg1 == "Blizzard_AuctionUI" ) then
			for i = 1, 8, 1 do
				bttn = _G["BrowseButton"..i.."Item"];
				if ( bttn ) then
					bttn:RegisterForClicks("LeftButtonUp", "MiddleButtonUp", "RightButtonUp");
					local func = bttn:GetScript("OnClick");
					if ( func ) then
						if ( not NuNHooks.NuNOri_AH_BrowseButtonItem_OnClick ) then
							NuNHooks.NuNOri_AH_BrowseButtonItem_OnClick = func;
						end
						bttn:SetScript("OnClick", NuN_AH_BrowseButton_OnClick);
					end
				end
			end
		elseif ( arg1 == "Blizzard_GuildUI" ) then
			-- these buttons didn't exist when we were first loaded, so the "parent" binding in our xml file couldn't be resolved; therefore, we need to set the
			-- correct parent now that they've been loaded.
			for btnIndex = 1, NuNC.MAX_GUILDROSTER_ROWS do
				nunGuildRosterItemButton = _G["NuN_GuildRosterButton" .. btnIndex];
				guildRosterItemButton = _G["GuildRosterContainerButton" .. btnIndex];
				if ( nunGuildRosterItemButton and guildRosterItemButton ) then
					nunGuildRosterItemButton:SetParent(guildRosterItemButton);
				end
			end
			
			GuildFrame = _G.GuildFrame;
			hooksecurefunc("GuildRoster_Update", NuNNew_GuildStatus_Update);
			hooksecurefunc("GuildRoster_UpdateTradeSkills", NuNNew_GuildStatus_Update);
			hooksecurefunc("GuildRosterButton_OnClick", NuNNew_GuildRosterButton_OnClick);
			if NuNHooks.NuNOriginal_GuildRoster_SetView == nil then
				NuNHooks.NuNOriginal_GuildRoster_SetView = GuildRoster_SetView;
				GuildRoster_SetView = NuN_InterceptGuildRoster_SetView;
				GuildRoster_SetView(GetCVar("guildRosterView"));
			end
		end
	-- Get Delayed Who Event information on players
	elseif ( ( event == "WHO_LIST_UPDATE" ) and ( NuN_WhoReturnStruct.func ) ) then		-- 5.60
		NuN_WhoReturnStruct.func();														-- 5.60
		NuN_WhoReturnStruct.func = nil;													-- 5.60
		NuN_WhoReturnStruct.name = nil;													-- 5.60
		NuN_WhoReturnStruct.timeLimit = nil;											-- 5.60
		NuN_WhoReturnStruct.secondTry = nil;
		NuN_suppressExtraWho = nil;
		if ( ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].alternativewho ) ) then
			SetWhoToUI(0);                                          					-- 5.60
			FriendsFrame:RegisterEvent("WHO_LIST_UPDATE");								-- 5.60
		end

	-- Update Ignores
	elseif ( event == "IGNORELIST_UPDATE" ) then
		if ( locals.NuN_IgnoreUpdate.func ) then
			local tDiff = GetTime();
			tDiff = tDiff - locals.NuN_IgnoreUpdate.time;
			if ( tDiff < 0.75 ) then
				local func = locals.NuN_IgnoreUpdate.func;
				func( locals.NuN_IgnoreUpdate.name );
			else
				locals.NuN_IgnoreUpdate.func = nil;
				locals.NuN_IgnoreUpdate.name = nil;
				locals.NuN_IgnoreUpdate.time = 0;
			end
		end

	-- this really needs tidying up, but I can't do it without testing :(
	elseif ( event == "PLAYER_ENTERING_WORLD" ) then
		if ( ( local_player.factionName == nil ) or ( NuN_horde == nil ) ) then
			NuN_getFaction();
		end
		if ( ( local_player.factionName ) and ( NuN_horde ) ) then
			NuN_AttemptedFriendIgnores = 0;
			friendsPendingUpdate = friendsPendingUpdate or NuN_Update_Friends();
			ignoresPendingUpdate = ignoresPendingUpdate or NuN_Update_Ignored();
		end
		
--		NuN_RegisterChatFilter();
	elseif ( event == "UPDATE_CHAT_WINDOWS" ) then
		NuN_RegisterChatFilter();
	-- update Friend notes
	elseif ( event == "FRIENDLIST_UPDATE" ) then
		if ( locals.NuN_FriendUpdate.func ) then
			local tDiff = GetTime();
			tDiff = tDiff - locals.NuN_FriendUpdate.time;
			if ( tDiff < 0.75 ) then
				local func = locals.NuN_FriendUpdate.func;
				func( locals.NuN_FriendUpdate.name );
			else
				locals.NuN_FriendUpdate.func = nil;
				locals.NuN_FriendUpdate.name = nil;
				locals.NuN_FriendUpdate.time = 0;
			end
		end

	-- player levelling up notes
	elseif ( ( event == "PLAYER_LEVEL_UP" ) and ( NuNSettings[local_player.realmName].autoN ) ) then
		local newLevel, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 = ...;
		local oldLevel = newLevel - 1;
		local levelUpName = locals.player_Name.." "..oldLevel.." - "..newLevel;
		if ( not NuNDataRNotes[levelUpName] ) then
			NuNF.NuN_CreateLevelUpNote(levelUpName, newLevel, arg2, arg3, arg5, arg6, arg7, arg8, arg9);
		end

	-- Mainly for auto noting people you Party with, and counting the number of times you have partied with them
	elseif ( ( event == "PARTY_MEMBERS_CHANGED" ) or ( event == "RAID_ROSTER_UPDATE" ) ) then
		if ( NuNSettings[local_player.realmName].autoP ) then
			NuN_ProcessParty();
		end
--[[
	elseif ( ( not NuN_State.NuN_QuestsUpdating ) and ( not NuN_State.NuN_AtStartup ) and ( event == "QUEST_LOG_UPDATE" ) ) then
		local hideFrame = nil;
		if ( ( NuNGNoteFrame:IsVisible() ) and ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "QST" ) ) then
--			NuNGNoteFrame:Hide();
			hideFrame = true;
		end
		-- i.e. if we have just clicked on a Quest Frame Accept button (NOT Quest Log.... Quest Frame)
		if ( NuN_QuestAccepted ) then
			local qHeader = nil;
			local qCollapsed = nil;
			local qIndex, qLevel, qTag, qComplete = NuNF.NuN_CheckQuestList(NuN_QuestAccepted);
			if ( qIndex > 0 ) then
				if ( hideFrame ) then NuNGNoteFrame:Hide(); end
				NuNF.NuN_ProcessQuest(NuN_QuestAccepted, qLevel, qTag, qHeader, qCollapsed, qComplete, qIndex, "Accepted");
			end
			NuN_QuestAccepted = nil;
			return;
		end
		if ( NuN_State.NuN_IgnoreNextQUpdate ) then
			NuN_State.NuN_IgnoreNextQUpdate = nil;
		elseif ( ( not NuN_QLF ) or ( not NuN_QLF:IsVisible() ) ) then
--			if ( hideFrame ) then NuNGNoteFrame:Hide(); end
			NuNF.NuN_UpdateQuestNotes(event);
		end
		NuN_QuestWatch_Update();
--]]
	-- This could be a problem if it gets triggered for every member in the Guild.... but if it gets triggered once when the Guild Roster is updated as a whole, then OK....
	elseif ( event == "GUILD_ROSTER_UPDATE" ) then
		if ( ( local_player.realmName ) and ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].autoGuildNotes ) ) then
			if ( locals.NuNDebug ) then
				nun_msgf("GUILD_ROSTER_UPDATE  syncGuildNotes:%s", tostring(NuN_State.NuN_syncGuildMemberNotes));
			end
			if ( NuN_State.NuN_syncGuildMemberNotes ) then
				NuN_State.NuN_syncGuildMemberNotes = false;
				if ( NuNSettings[local_player.realmName].autoGRVerbose ) then
					NuN_SyncGuildMemberNotes("Startup");
				else
					NuN_SyncGuildMemberNotes();
				end

			else
				NuN_SyncGuildMemberNotes( NuNSettings[local_player.realmName].autoGRVerbose );
			end
		end

	elseif ( event == "INSPECT_READY" ) then
		NuNF.QueryTalents();

	end
end

local function NuN_OnEnterPlayerRatingMenuItem(frame, motion)
	-- ignore any menu items that we didn't initialize, as the buttons are reused.
	if frame.arg2 and frame.arg2 == "NUN_PR" then
--		nun_msgf(">>> NuN_OnEnterPlayerRatingMenuItem - frame:%s    motion:%s    UIDROPDOWNMENU_MENU_LEVEL:%s", tostring(frame:GetName()), tostring(motion), tostring(UIDROPDOWNMENU_MENU_LEVEL));
		GameTooltip:Hide();
		NuN_DisplayTooltip(frame:GetParent(), frame.arg1, true);
	end
end
local function NuN_OnLeavePlayerRatingMenuItem(frame, motion)
	-- ignore any menu items that we didn't initialize
	if frame.arg2 and frame.arg2 == "NUN_PR" then
--		nun_msgf("<<< NuN_OnEnterPlayerRatingMenuItem - frame:%s    motion:%s", tostring(frame:GetName()), tostring(motion));
		NuN_Tooltip:Hide();
	end
end

--[[
func:			the function to call when an item is clicked
u_name:			the name of the unit we're setting the rating for
parentMenuItem:	the text that corresponds to our parent menu item [only relevant for context menus]
--]]
local isHooked = {}
local function NuN_BuildPlayerRatingsSubmenu(func, u_name, parentMenuItem)
--	nun_msgf("NuN_BuildPlayerRatingsSubmenu - func:%s   u_name:%s   parentMenuItem:%s",
--		tostring(func), tostring(u_name), tostring(parentMenuItem));
	if ( NuNSettings.ratings ) then
		local currentValueIdx = nil;

		-- if we have a note for this player with a rating already set, enable checked on the corresponding menu item
		if u_name and locals.NuNDataPlayers[u_name] and locals.NuNDataPlayers[u_name].prating then
			currentValueIdx = locals.NuNDataPlayers[u_name].prating;
		end
		local contextMenuIndexOffset = 0;
		if parentMenuItem ~= nil then
			contextMenuIndexOffset = 1;
		end
		local num_ratings, num_originalRatings = getn(NuNSettings.ratings), getn(NUN_ORATINGS);
		local newlyHooked = 0;
		local info = UIDropDownMenu_CreateInfo();
		for i=1, num_ratings, 1 do
			info.func = func;
			info.owner = parentMenuItem;
	
			info.text = NuNSettings.ratings[i];
			if parentMenuItem == nil then
				-- this is a combobox drop down, not a context menu
				info.tooltipTitle = NuNSettings.ratings[i];
				info.tooltipText = NuNSettings.ratingsT[i];
			end
			local tooltipTag = "NuNPlayerRating" .. i .. "TT";
			info.arg1 = tooltipTag;
			info.arg2 = "NUN_PR";
			info.value = NuNSettings.ratings[i];
			info.checked = nil;
			info.notCheckable = nil;
			if currentValueIdx and i == currentValueIdx then
				info.checked = 1;
			end
			
			UIDropDownMenu_AddButton(info, UIDROPDOWNMENU_MENU_LEVEL);
			
			if not isHooked[UIDROPDOWNMENU_MENU_LEVEL] or isHooked[UIDROPDOWNMENU_MENU_LEVEL] == 0 then
				newlyHooked = 1;
				NuNC[tooltipTag .. 1] = NuNSettings.ratings[i];
				NuNC[tooltipTag .. 2] = NuNSettings.ratingsT[i];
				NuNC[tooltipTag .. 3] = nil;
				NuNC[tooltipTag .. 4] = nil;
				NuNC[tooltipTag .. 5] = nil;
				NuNC[tooltipTag .. 6] = nil;
				
				if parentMenuItem == nil then
					NuNC[tooltipTag .. 3] = "\n";
					-- add instructions for how to change the name of the rating
					NuNC[tooltipTag .. 4] = NUN_CHANGE_RATING;
					if i < num_originalRatings + 1 and NuNSettings.ratings[i] ~= NUN_ORATINGS[i] then
						-- if the user modified the rating title, add instructions for resetting it to the tooltip.
						NuNC[tooltipTag .. 5] = NUN_CHANGE_RATING_CUSTOM;
					end
				end

				-- if the user has newbie tips turned off (which I assume most of our users will do), then they won't be able to see any tooltips for menu items
				-- so hook onto the enter/leave events for the individual menu items so that we can show our own tooltip
				local buttonName = "DropDownList" .. UIDROPDOWNMENU_MENU_LEVEL .. "Button" .. i + contextMenuIndexOffset;
				local menuItemButton = _G[buttonName];
				menuItemButton:HookScript("OnEnter", NuN_OnEnterPlayerRatingMenuItem);
				menuItemButton:HookScript("OnLeave", NuN_OnLeavePlayerRatingMenuItem);
			end
		end
		isHooked[UIDROPDOWNMENU_MENU_LEVEL] = isHooked[UIDROPDOWNMENU_MENU_LEVEL] or newlyHooked;
	end
end

-- Lots of Drop Down Box set up routines
function NuNPRatingDropDown_Initialise()
	NuN_BuildPlayerRatingsSubmenu(NuNF.NuNPRatingsButton_OnClick);
end

-- RatingModifier
function NuNF.NuNPRatingsButton_OnClick(self)
	chosenRating = self:GetID();
	oldRating = UIDropDownMenu_GetSelectedID( NuNPRatingDropDown );
	if ( chosenRating == oldRating ) then
		oldRating = true;
	else
		oldRating = false;
	end
	ratingChosen = false;

	if ( IsControlKeyDown() ) then
		if ( chosenRating < maxRatings ) then
			StaticPopup_Show("NUN_CHANGE_RATING_ONE");
		end

	elseif ( IsAltKeyDown() ) then
		if ( chosenRating < maxRatings ) then
			NuNSettings.ratings[chosenRating] = NUN_ORATINGS[chosenRating];
			NuNSettings.ratingsT[chosenRating] = NUN_ORATINGS_TEXT[chosenRating];
			NuNSettings.ratingsBL[chosenRating] = 0;
			NuN_SetupRatings();
			curRatingTxt = UIDropDownMenu_GetText( NuNPRatingDropDown );
			if ( ( curRatingTxt ) and ( curRatingTxt ~= "" ) and ( oldRating ) ) then
				UIDropDownMenu_SetText(NuNPRatingDropDown, NUN_ORATINGS[chosenRating]);
			end
		end

	else
		UIDropDownMenu_SetSelectedID(NuNPRatingDropDown, chosenRating);
		locals.dropdownFrames.ddPRating = chosenRating;
		locals.lastDD = "PRating";
		NuNButtonClrDD:Enable();
		if ( BlackList ) then
			NuN_BlackList(local_player.currentNote.unit, chosenRating);
		end
	end
end

--local UIDropDownMenuHelper = CreateFrame("Frame");
--UIDropDownMenuHelper:SetScript("OnAttributeChanged", UIDropDownMenuDelegate_OnAttributeChanged);
function NuNDropDownMenu_Initialize(frame, func, ...)
	-- unfortunately, this doesn't work..  :\
	-- but I'll leave this in so at least we know exactly where the dropdown menu is being tainted
	-- I believe there's a way to hook into the dropdown menu for the unit frames without tainting which involves using restricted environment.
--	UIDropDownMenuHelper:SetAttribute("openmenu", frame);
--[===[@debug@
--	local iname, oname;
--	if UIDROPDOWNMENU_OPEN_MENU then oname = UIDROPDOWNMENU_OPEN_MENU.name; end
--	if UIDROPDOWNMENU_INIT_MENU then iname = UIDROPDOWNMENU_INIT_MENU.name; end
--	nun_msgf("frame:%s (%s)   initmenu:%s  openmenu:%s", tostring(frame.name),tostring(frame),tostring(iname),tostring(oname));
--@end-debug@]===]
--	func(frame, ...)
	-- if the frame had an existing handler for OnShow(), call it now
	if frame.origOnShow then
--nun_msgf("frame %s has an existing OnShow:%s", tostring(frame:GetName()), tostring(frame.origOnShow:GetName()));
		frame.origOnShow(frame, ...)
	end
	UIDropDownMenu_Initialize(frame, func);
end

function NuNHRaceDropDown_Initialise(frame)
--nun_msgf("NuNHRaceDropDown_Initialise - frame:%s", tostring(frame:GetName()));
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_HRACES), 1 do				-- #NUN_HRACES
		info.func = NuNF.NuNHRaceButton_OnClick;
		info.text = NUN_HRACES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNHRaceButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNHRaceDropDown, self:GetID());
	locals.dropdownFrames.ddRace = self:GetID();
	locals.lastDD = "Race";
	NuNButtonClrDD:Enable();
end

function NuNARaceDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_ARACES), 1 do				-- #NUN_ARACES
		info.func = NuNF.NuNARaceButton_OnClick;
		info.text = NUN_ARACES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNARaceButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNARaceDropDown, self:GetID());
	locals.dropdownFrames.ddRace = self:GetID();
	locals.lastDD = "Race";
	NuNButtonClrDD:Enable();
end

function NuNHClassDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_HCLASSES), 1 do						 -- #NUN_HCLASSES
		info.func = NuNF.NuNHClassButton_OnClick;
		info.text = NUN_HCLASSES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNHClassButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNHClassDropDown, self:GetID());
	locals.dropdownFrames.ddClass = self:GetID();
	locals.lastDD = "Class";
	NuNButtonClrDD:Enable();
end


function NuNAClassDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_ACLASSES), 1 do						-- #NUN_ACLASSES
		info.func = NuNF.NuNAClassButton_OnClick;
		info.text = NUN_ACLASSES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNAClassButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNAClassDropDown, self:GetID());
	locals.dropdownFrames.ddClass = self:GetID();
	locals.lastDD = "Class";
	NuNButtonClrDD:Enable();
end



function NuNSexDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_SEXES), 1 do						-- #NUN_SEXES
		info.func = NuNF.NuNSexButton_OnClick;
		info.text = NUN_SEXES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNSexButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNSexDropDown, self:GetID());
	locals.dropdownFrames.ddSex = self:GetID();
	locals.lastDD = "Sex";
	NuNButtonClrDD:Enable();
end



function NuNProf1DropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_PROFESSIONS), 1 do					-- #NUN_PROFESSIONS
		info.func = NuNF.NuNProf1Button_OnClick;
		info.text = NUN_PROFESSIONS[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNProf1Button_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNProf1DropDown, self:GetID());
	locals.dropdownFrames.ddProf1 = self:GetID();
	locals.lastDD = "Prof1";
	NuNButtonClrDD:Enable();
end




function NuNProf2DropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_PROFESSIONS), 1 do					-- #NUN_PROFESSIONS
		info.func = NuNF.NuNProf2Button_OnClick;
		info.text = NUN_PROFESSIONS[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNProf2Button_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNProf2DropDown, self:GetID());
	locals.dropdownFrames.ddProf2 = self:GetID();
	locals.lastDD = "Prof2";
	NuNButtonClrDD:Enable();
end




function NuNArenaRDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_ARENAR), 1 do
		info.func = NuNF.NuNArenaRButton_OnClick;
		info.text = NUN_ARENAR[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNArenaRButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNArenaRDropDown, self:GetID());
	locals.dropdownFrames.ddArena = self:GetID();
	locals.lastDD = "Arena";
	NuNButtonClrDD:Enable();
end



function NuNHHRankDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_HRANKS), 1 do						-- #NUN_HRANKS
		info.func = NuNF.NuNHHRankButton_OnClick;
		info.text = NUN_HRANKS[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNHHRankButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNHHRankDropDown, self:GetID());
	locals.dropdownFrames.ddHRank = self:GetID();
	locals.lastDD = "HRank";
	NuNButtonClrDD:Enable();
end



function NuNAHRankDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_ARANKS), 1 do						-- #NUN_ARANKS
		info.func = NuNF.NuNAHRankButton_OnClick;
		info.text = NUN_ARANKS[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNAHRankButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNAHRankDropDown, self:GetID());
	locals.dropdownFrames.ddHRank = self:GetID();
	locals.lastDD = "HRank";
	NuNButtonClrDD:Enable();
end




function NuNOptionsSearchDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_SEARCHFOR), 1 do						-- #NUN_SEARCHFOR
		info.func = NuNF.NuNOptionsSearchDropDown_OnClick;
		info.text = NUN_SEARCHFOR[i].Display;
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNOptionsSearchDropDown_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNOptionsSearchDropDown, self:GetID());
	locals.dropdownFrames.ddSearch = self:GetID();
end




function NuNSearchClassDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_ALLCLASSES), 1 do						-- #NUN_ALLCLASSES
		info.func = NuNF.NuNSearchClassButton_OnClick;
		info.text = NUN_ALLCLASSES[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNSearchClassButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNSearchClassDropDown, self:GetID());
	ddClassSearch = self:GetID();
	locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "Class");
	locals.searchType = "Class";
	NuNSearch_Search();
end




function NuNSearchProfDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_PROFESSIONS), 1 do						-- #NUN_PROFESSIONS
		info.func = NuNF.NuNSearchProfButton_OnClick;
		info.text = NUN_PROFESSIONS[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNSearchProfButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNSearchProfDropDown, self:GetID());
	ddProfSearch = self:GetID();
	locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "Profession");
	locals.searchType = "Profession";
	NuNSearch_Search();
end




function NuNSearchQHDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(AltArray), 1 do
		info.func = NuNF.NuNSearchQHButton_OnClick;
		info.text = AltArray[i].displayName;						-- 5.60  Make sure AltArray[1] = locals.player_Name
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNSearchQHButton_OnClick(self,btn,down)
	ddQHSearch = self:GetID();
	UIDropDownMenu_SetSelectedID(NuNSearchQHDropDown, ddQHSearch);
	locals.questHistory.Realm = AltArray[ddQHSearch].realm;
	locals.questHistory.Tag = AltArray[ddQHSearch].name;
	locals.questHistory.Index = ddQHSearch;
	locals.questHistory.Title = AltArray[ddQHSearch].displayName;
	NuNQuestHistory = NuNData[locals.questHistory.Realm].QuestHistory[locals.questHistory.Tag];
--	locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "Quest History");		-- no need to update this, should already be Quest History command...
--	locals.searchType = "Quest History";
	NuN_FetchQuestHistory();
end




function NuNChatDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_TRANSMITTO), 1 do							-- #NUN_TRANSMITTO
		info.func = NuNF.NuNChatButton_OnClick;
		info.text = NUN_TRANSMITTO[i].Display;
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.SetDropDownSelectedID(frame, id, level, refresh)
	frame.selectedID = id;
	frame.selectedName = nil;
	frame.selectedValue = nil;
	if not level then level = 1 end
	
	UIDropDownMenu_Refresh(frame, useValue, level);
end

function NuNF.NuNChatButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNChatDropDown, self:GetID());
	
	locals.sendTo = NUN_TRANSMITTO[self:GetID()].Command;
	if ( locals.sendTo == "WHISPER" ) or ( locals.sendTo == "NuN" ) then
		NuNChatTextBox:SetText("");
		NuNChatTextBox:Show();
		NuNTransmit:Disable();
		NuNChannelDropDown:Hide();
	elseif ( locals.sendTo == "CHANNEL" ) then
		NuNChatTextBox:Hide();
		NuNTransmit:Disable();
		NuNChannelDropDown:Show();
	else
		if ( NuNChatTextBox:IsVisible() ) then
			NuNChatTextBox:Hide();
		end
		if ( NuNChannelDropDown:IsVisible() ) then
			NuNChannelDropDown:Hide();
		end
		if ( busySending.active ) then
			NuNTransmit:Disable();
		else
			NuNTransmit:Enable();
		end
	end
end


--TODO : Possible problem with Arg1 in array code, revisit if errors still occur
function NuNChannelDropDown_Initialise(frame)
	local counter = 1;
	local channelArray = { GetChannelList() };
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(channelArray), 2 do						-- #channelArray
		local cIndex, cName = GetChannelName(channelArray[i]);
		info.func = NuNF.NuNChannelButton_OnClick;
		info.text = channelArray[i]..". "..cName;
		info.arg1 = channelArray[i];
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNChannelButton_OnClick(self, arg1)
	UIDropDownMenu_SetSelectedID(NuNChannelDropDown, self:GetID());
	sendToChannel.id = arg1;
	sendToChannel.name = self:GetText();
	if ( busySending.active ) then
		NuNTransmit:Disable();
	else
		NuNTransmit:Enable();
	end
end



function NuNGTypeDropDown_Initialise(frame)
	local info = UIDropDownMenu_CreateInfo();
	for i=1, getn(NUN_NOTETYPES), 1 do							-- #NUN_NOTETYPES
		info.func = NuNF.NuNGTypeButton_OnClick;
		info.text = NUN_NOTETYPES[i].Display;
		info.checked = nil;
		UIDropDownMenu_AddButton(info);
	end
end

function NuNF.NuNGTypeButton_OnClick(self,btn,down)
	UIDropDownMenu_SetSelectedID(NuNGTypeDropDown, self:GetID());
	NuNGNoteFrame.type = self:GetID();
	if ( NUN_NOTETYPES[NuNGNoteFrame.type].Command == "NPC" ) then
		NuNNPCTargetButton:Show();
	else
		NuNNPCTargetButton:Hide();
	end
end


-- This function is for changing the details of the User Definable Buttons on the Contact Note Frame
function NuNEditDetails()
	local prntObj;
	local prntTxtObj;

	local newTxt = (NuNEditDetailsBox:GetText());

	if (newTxt ~= locals.originalText) then
		local chldObj = _G["NuNInforButton"..bttnNumb];
		if ((newTxt == "") and (locals.isTitle)) or ((newTxt == nil) and (locals.isTitle)) then
			_G["NuNInforButton"..bttnNumb.."ButtonTextDetail"]:SetText("");		-- 5.60
			chldObj:Disable();
		else
			chldObj:Enable();
		end
		bttnTxtObj:SetText(newTxt);

		if (locals.isTitle) then
			if ( NuNEditDetail_CheckButton:GetChecked() ) then
				locals.headingNumber = locals.pHead..bttnNumb;
				locals.headingName = local_player.currentNote.unit..locals.headingNumber;
				if (not NuNSettings[local_player.realmName][locals.headingNumber]) then
					NuNSettings[local_player.realmName][locals.headingNumber] = {};
				end
				NuNSettings[local_player.realmName][locals.headingNumber].txt = newTxt;
				if ( locals.NuNDataPlayers[locals.headingName] ) then	-- As we have set a new Account Default while viewing this Player, then nil any Player specific setting for this player
					locals.NuNDataPlayers[locals.headingName] = nil;	-- this does not reset all player specific settings.
				end
			else
				idx = tonumber(locals.bttnNumb);
				if ( ( newTxt == "" ) or ( newTxt == nil ) ) then
					locals.bttnChanges[idx] = -1;						-- 5.60 Use -1 to flag blank
				else
					locals.bttnChanges[idx] = newTxt;
				end
			end
		else
			idx = locals.bttnNumb + locals.detlOffset;
			if ( newTxt == "" ) then
				locals.bttnChanges[idx] = -1;							-- 5.60 Use -1 to flag blank
			else
				locals.bttnChanges[idx] = newTxt;
			end
			if ( idx == (locals.detlOffset + 1) ) then					-- really not sure if this setting of contact.guild is necessary; seems a cart before horse way of setting the data.... ?
				prntTxtObj = _G["NuNTitleButton"..bttnNumb.."ButtonTextHeading"];
				if ( prntTxtObj:GetText() == NUN_DFLTHEADINGS[1] ) then
					contact.guild = newTxt;
				end
			end
		end
		NuNButtonSaveNote:Enable();
		NuNEditDetailsFrame:Hide();
	else
		NuNButtonSaveNote:Enable();
		NuNEditDetailsFrame:Hide();
	end
end



-- User Definable Buttons on Click
function NuNUserButton_OnClick(bttn)
	local bttnName = bttn:GetName();
	local prfx = (strsub(bttnName,  1, 8));

	locals.bttnNumb = (strsub(bttnName, 15,  15));

	if (prfx == "NuNTitle") then
		locals.isTitle = true;
		bttnTxtObj = _G[bttnName.."ButtonTextHeading"];
	else
		locals.isTitle = false;
		bttnTxtObj = _G[bttnName.."ButtonTextDetail"];
	end
	locals.originalText = bttnTxtObj:GetText();

	NuNEditDetails_ShowFrame(locals.isTitle);
end


-- for editing User Definable Buttons
function NuNEditDetails_ShowFrame(_isTitle)
	NuNButtonSaveNote:Disable();

	NuNText:ClearFocus();
	if (locals.originalText == nil) then
		NuNEditDetailsBox:SetText("");
	else
		NuNEditDetailsBox:SetText(locals.originalText);
	end
	if (_isTitle) then
		NuNCheckBoxLabel:SetText("Save as Default");
		NuNEditDetail_CheckButton:SetChecked(0);
		NuNEditDetailsRestoreButton:Enable();
		NuNEditDetail_CheckButton:Show();
		NuNEditDetailsRestoreButton:Show();
	else
		NuNCheckBoxLabel:SetText("");
		NuNEditDetail_CheckButton:Hide();
		NuNEditDetailsRestoreButton:Hide();
	end
	NuNEditDetailsFrame:Show();
	NuNEditDetailsBox:SetFocus();
end



function NuN_EditDetailCheckButtonOnClick()
	if ( NuNEditDetail_CheckButton:GetChecked() ) then
		NuNEditDetailsRestoreButton:Disable();
	else
		NuNEditDetailsRestoreButton:Enable();
	end
end



function NuNEditDetailsRestore()
	locals.headingName = local_player.currentNote.unit .. locals.pHead .. locals.bttnNumb;
	if ( locals.NuNDataPlayers[locals.headingName] ) then
		locals.NuNDataPlayers[locals.headingName] = nil;
	end
	NuNButtonSaveNote:Enable();
	NuNEditDetailsFrame:Hide();
	NuNEditDetailsFrame:Hide();
	HideNUNFrame();
	NuN_ShowNote();
end





-------------------------------------------------------------------------------------------
-- Succesful Function Hooks



function NuNNew_FriendsFrameFriendButton_OnClick(btn)
 	if ( ( btn == "LeftButton" ) and ( NuNFrame:IsVisible() ) ) then
 		NuNEditDetailsFrame:Hide();
		HideNUNFrame();
 		NuN_ShowFriendNote();
 	end
end

function NuNNew_FriendsFrameIgnoreButton_OnClick(clickedButton)
	if ( NuNFrame:IsVisible() ) then
		NuNEditDetailsFrame:Hide();
		HideNUNFrame();
		NuN_ShowIgnoreNote(clickedButton);
	end
end

function NuNNew_GuildRosterButton_OnClick(btn)
	if ( ( btn == "LeftButton" ) and ( NuNFrame:IsVisible() ) ) then
		NuNEditDetailsFrame:Hide();
		HideNUNFrame();
		NuN_ShowGuildNote();
	end
end

function NuNNew_FriendsFrameWhoButton_OnClick(btn)
	if ( ( btn == "LeftButton" ) and ( NuNFrame:IsVisible() ) ) then
		NuNEditDetailsFrame:Hide();
		HideNUNFrame();	
		NuN_ShowWhoNote(WhoFrame.selectedName);
	end
end

function NuNNew_FriendsList_Update()
	local bttnIndx;

	for i = 1, 10, 1 do
		bttnIndx = _G["NuN_FriendNotesButton"..i];
		NuN_UpdateNoteButton(bttnIndx, i, NuNC.UPDATETAG_FRIEND);
	end
end

--[[
	@return	numIgnores			[i]
			numBlocks			[i]
			numMutes			[i[
			numToonBlocks		[i]
			ignoredHeader		[i]
			blockedHeader		[i]
			mutedHeader			[i]
			blockedToonHeader	[i]
--]]
function NuN_RetrieveIgnoreListParams()
	-- this whole copy/paste thing we have to do from blizzard's code is really annoying....doesn't their UI guy know any OOP?
	local numIgnores, numBlocks, numMutes, numToonBlocks = 0, 0, 0, 0;
	if ( IsVoiceChatEnabled() ) then
		numMutes = GetNumMutes();
	end
	
	-- Headers stuff
	local ignoredHeader, blockedHeader, mutedHeader, blockedToonHeader = 0, 0, 0, 0;
	if ( numIgnores > 0 ) then ignoredHeader = 1; end	
	if ( numBlocks > 0 ) then blockedHeader = 1; end
	if ( numToonBlocks > 0 ) then blockedToonHeader = 1; end	
	if ( numMutes > 0 ) then mutedHeader = 1; end
	
	return numIgnores, numBlocks, numMutes, numToonBlocks, ignoredHeader, blockedHeader, mutedHeader, blockedToonHeader;
end

function NuNNew_IgnoreList_Update()
	local numIgnores, numBlocks, numMutes, numToonBlocks, ignoredHeader, blockedHeader, mutedHeader, blockedToonHeader
		= NuN_RetrieveIgnoreListParams();
	local lastIgnoredIndex = numIgnores + ignoredHeader;
	local lastBlockedIndex = lastIgnoredIndex + numBlocks + blockedHeader;
	local lastBlockedToonIndex = lastBlockedIndex + numToonBlocks + blockedToonHeader;
	local lastMutedIndex = lastBlockedToonIndex + numMutes + mutedHeader;
	local numEntries = lastMutedIndex;
--NuN_Message("numIgnores:"..tostring(numIgnores).."  lastIgnoredIndex:"..tostring(lastIgnoredIndex)..
--			"lastBlockedIndex:"..tostring(lastBlockedIndex).."  lastMutedIndex:"..tostring(lastMutedIndex)..
--			"blockedHeader:"..tostring(blockedHeader).." mutedHeader:"..tostring(mutedHeader));
--@{
-- 3.3.5 changed the ignore list so that the first "button" in the list is now a "header" button
-- evo: fixed ignore list not updating idx correctly when TopItem was different from FirstItem
	local scrollOffset = FauxScrollFrame_GetOffset(FriendsFrameIgnoreScrollFrame);
	local ignoreButton, ignoredItemIndex, ignoreNoteButton;
	for i = 1, IGNORES_TO_DISPLAY, 1 do
		-- but the actual buttons are still 1-based
		ignoredItemIndex = i + scrollOffset;
--evo 
--NuN_Message("NuNNew_IgnoreList_Update - ["..tostring(i).." ("..tostring(scrollOffset)..")]: ignoredItemIndex:"..tostring(ignoredItemIndex));
		ignoreButton = _G["FriendsFrameIgnoreButton" .. i];
		-- for the list items which correspond to headers (i.e. Ignored, Muted, etc.), they are made children of the button for that list item.  
		if	ignoreButton ~= FriendsFrameIgnoredHeader:GetParent() and
			ignoreButton ~= FriendsFrameBlockedInviteHeader:GetParent() and
			ignoreButton ~= FriendsFrameBlockedToonHeader:GetParent() and
			ignoreButton ~= FriendsFrameMutedHeader:GetParent() then
			if ignoredItemIndex <= lastIgnoredIndex then
				ignoredItemIndex = ignoredItemIndex - ignoredHeader;
			elseif ( blockedHeader == 1 and ignoredItemIndex == lastIgnoredIndex + 1 ) then
				-- blocked header
			elseif ( ignoredItemIndex <= lastBlockedIndex ) then
				-- blocked
				ignoredItemIndex = ignoredItemIndex - lastIgnoredIndex - blockedHeader;
				--local blockID, blockName = BNGetBlockedInfo(button.index);
			elseif ( blockedToonHeader == 1 and ignoredItemIndex == lastBlockedIndex + 1 ) then
				-- blocked TOON header
			elseif ( ignoredItemIndex <= lastBlockedToonIndex ) then
				-- blocked TOON
				ignoredItemIndex = ignoredItemIndex - lastBlockedIndex - blockedToonHeader;
				--local blockID, blockName = BNGetBlockedToonInfo(button.index);
			elseif ( mutedHeader == 1 and ignoredItemIndex == lastBlockedToonIndex + 1 ) then
				-- muted header
			elseif ( ignoredItemIndex <= lastMutedIndex ) then
				-- muted
				ignoredItemIndex = ignoredItemIndex - lastBlockedToonIndex - mutedHeader;
			end
--nun_msgf("   ignoredItemIndex is now %i  (lastIgnoredIndex:%i  lastBlockedIndex:%i)", ignoredItemIndex, lastIgnoredIndex, lastBlockedIndex);
			ignoreNoteButton = _G["NuN_IgnoreNotesButton"..i];
			ignoreNoteButton:Show();
			NuN_UpdateNoteButton(ignoreNoteButton, i, NuNC.UPDATETAG_IGNORE);
		else
			ignoreNoteButton = _G["NuN_IgnoreNotesButton"..i];
			if ignoreNoteButton then
				-- this is a button to open a notes panel, but the item it's linked to is only a header, so hide it.
				ignoreNoteButton:Hide();
			end
		end
	end
end

function NuN_InterceptGuildRoster_SetView( newView )
	locals.currentGuildRosterView = newView or NuNC.DEFAULT_GUILDROSTERVIEW;
	
	NuNHooks.NuNOriginal_GuildRoster_SetView(newView);
	NuNNew_GuildStatus_Update();
end

function NuNNew_GuildStatus_Update()
	local nunItemButton;

	if locals.currentGuildRosterView == nil then
		locals.currentGuildRosterView = GetCVar("guildRosterView");
		if locals.currentGuildRosterView == nil then
			locals.currentGuildRosterView = NuNC.DEFAULT_GUILDROSTERVIEW;
		end
	end
--	nun_msgf("NuNNew_GuildStatus_Update.........%s...........", locals.currentGuildRosterView);

	if GuildFrame then
		for i = 1, NuNC.MAX_GUILDROSTER_ROWS, 1 do
			nunItemButton = _G["NuN_GuildRosterButton"..i];
			if nunItemButton then
				NuN_UpdateNoteButton(nunItemButton, i, NuNC.UPDATETAG_GUILD_ROSTER);
			end
		end
	end
end

function NuNNew_WhoList_Update()
	local bttnIndx;

	for i = 1, 17, 1 do
		bttnIndx = _G["NuN_WhoNotesButton"..i];
		if bttnIndx then
			NuN_UpdateNoteButton(bttnIndx, i, NuNC.UPDATETAG_WHO);
		end
	end
end

function NuNNew_QuestLog_Update()
if ron_disabled then
	
	local bttnIndx;

	NuNF.NuN_QuestLogButtons();
	
	for i = 1, NuNC.NUN_QUESTLOG_BUTTONS, 1 do
		bttnIndx = _G["NuN_QuestNotesButton"..i];
		if ( bttnIndx ) then
			NuN_UpdateNoteButton(bttnIndx, i, NuNC.NUN_QUEST_C);
		else
--			NuN_Message(NuNC.NUN_QUESTLOG_BUTTONS .. " | " .. i);
		end
	end
end
end


function NuN_AH_BrowseButton_OnClick(self,btn,down)
	NuNHooks.NuNOri_AH_BrowseButtonItem_OnClick(self,btn,down);

	if ( ( NuNSettings[local_player.realmName].modifier == "on" ) and ( IsNuNModifierKeyDown(down) ) ) then
		local myParent = self:GetParent();
		local itmLink = GetAuctionItemLink("list", myParent:GetID() + FauxScrollFrame_GetOffset(BrowseScrollFrame));
		itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\|", ":0:0:0:0:0:0:0|");
		itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\124", ":0:0:0:0:0:0:0\124");

		if ( ( itmLink ~= nil ) and ( itmLink ~= "" ) ) then
			if ( ( NuNGNoteFrame:IsVisible() ) or ( NuNFrame:IsVisible() ) ) then
				if ( NuNGNoteFrame:IsVisible() ) then
					NuNGNoteTextScroll:Insert(itmLink);				-- + v5.00.11200
					StackSplitFrame:Hide();
					return true;
				elseif ( NuNFrame:IsVisible() ) then
					NuNText:Insert(itmLink);					-- + v5.00.11200
					StackSplitFrame:Hide();
					return true;
				end
			else
				NuNGNoteFrame.fromQuest = nil;
				if ( NuNData[locals.itmIndex_dbKey][itmLink] ) then
					itmLink = ( NuNData[locals.itmIndex_dbKey][itmLink] );
				end
				if ( ( NuNDataRNotes[itmLink] ) or ( NuNDataANotes[itmLink] ) ) then
					local_player.currentNote.general = itmLink;
					NuN_ShowSavedGNote();
					StackSplitFrame:Hide();
					return true;
				else
					NuNF.NuN_GNoteFromItem(itmLink, "GameTooltip");
					StackSplitFrame:Hide();
					return true;
				end
			end
		end
	end
end

function NuNNew_ContainerFrameItemButton_OnModifiedClick(self, btn)
	if ( ( NuNSettings[local_player.realmName].modifier == "on" ) and ( IsNuNModifierKeyDown(btn) ) ) then
		if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then

		else
			local itmLink = GetContainerItemLink( self:GetParent():GetID(), self:GetID() );
			itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\|", ":0:0:0:0:0:0:0|");
			itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\124", ":0:0:0:0:0:0:0\124");

			if ( ( itmLink ~= nil ) and ( itmLink ~= "" ) ) then
				if ( ( NuNGNoteFrame:IsVisible() ) or ( NuNFrame:IsVisible() ) ) then
					if ( NuNGNoteFrame:IsVisible() ) then
						NuNGNoteTextScroll:Insert(itmLink);				-- + v5.00.11200
						StackSplitFrame:Hide();
						return true;
					elseif ( NuNFrame:IsVisible() ) then
						NuNText:Insert(itmLink);					-- + v5.00.11200
						StackSplitFrame:Hide();
						return true;
					end
				else
					NuNGNoteFrame.fromQuest = nil;
					if ( NuNData[locals.itmIndex_dbKey][itmLink] ) then
						itmLink = ( NuNData[locals.itmIndex_dbKey][itmLink] );
					end
					if ( ( NuNDataRNotes[itmLink] ) or ( NuNDataANotes[itmLink] ) ) then
						local_player.currentNote.general = itmLink;
						NuN_ShowSavedGNote();
						StackSplitFrame:Hide();
						return true;
					else
						NuNF.NuN_GNoteFromItem(itmLink, "GameTooltip");
						StackSplitFrame:Hide();
						return true;
					end
				end
			end
		end
	end
end

local function DecodeHyperlink(hyperlink)
	if hyperlink and type(hyperlink) == "string" then
		hyperlink = strgsub(hyperlink, "\124", "\124\124");
	end
	return tostring(hyperlink);
end
function NuN_OnHyperlinkShow(chatFrame, link, text, button)
	if locals.NuNDebug and chatFrame == DEFAULT_CHAT_FRAME and locals.debugging_msg_hooks then
		nun_msgf(" >>>NuN_OnHyperlinkShow<<< chatFrame:%s    link:%s     text:%s     button:%s", tostring(chatFrame:GetName()), DecodeHyperlink(link), DecodeHyperlink(text), tostring(button));
	end
end

locals.showColoredNameDebug = nil;
function NuN_GetColoredName(event, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12)
	local showDebug = locals.showColoredNameDebug and locals.debugging_msg_hooks;
	locals.showColoredNameDebug = nil;
	
	arg2 = NuNHooks.NuNOriginal_GetColoredName(event, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);
	if arg2 then
		local origArg2 = arg2;
		arg2 = NotesUNeed.NuN_Statics.TagPlayerChatName(arg2, showDebug);
		if locals.NuNDebug and showDebug then
			nun_msgf("NuN_GetColoredName - origArg2:%s    arg2:%s (%s)", origArg2, arg2, DecodeHyperlink(arg2));
		end
	end
	
	return arg2;
end

function NuN_ChatFrameOnHyperlinkShow(chatframe, link, text, buttonName)
	local processedByNuN = NuNNew_SetItemRef(chatframe, link, text, buttonName);
	if processedByNuN ~= true then
		NuNHooks.NuNOriginal_OnHyperlinkShow(chatframe, link, text, buttonName);
	end
	
	if ( ItemRefTooltip:IsVisible() ) then
		NuN_ItemRefTooltip_OnShow();
	end	
end

function NuNNew_SetItemRef(self, link, text, btn)
--nun_msgf("SetItemRef - link:%s  text:%s  btn:%s", DecodeHyperlink(link),  DecodeHyperlink(text), tostring(btn));
	local processed = false;
	if ( NuNSettings[local_player.realmName].modifier == "on" ) then
		if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
			-- nothing
		elseif ( strsub(link, 1, 6) == "player" or strsub(link, 1, 3) == "NuN" or strsub(link, 1, 9) == "HBNplayer" ) then
			local _name, linkTypeNuN;
			if ( strsub(link, 1, 3) == "NuN" ) then
				linkTypeNuN = true;
				_name = strsub(link, 5);
			elseif strsub(link, 1, 9) == "HBNplayer" then
			
			else
				_name = strsub(link, 8);
			end
			if ( _name and (strlen(_name) > 0) ) then
				local uid = strfind(_name, ":");
				if ( uid ) then
					_name = strsub(_name, 1, uid-1);
				end
				if ( IsNuNModifierKeyDown(btn) or linkTypeNuN ) then
					if ( locals.NuNDataPlayers[_name] ) then
						NuN_ShowSavedNote(_name);
					else
						NuN_CreateContact(_name, local_player.factionName);
					end
					if ( DEFAULT_CHAT_FRAME.editBox ) then
						ChatEdit_OnEscapePressed(DEFAULT_CHAT_FRAME.editBox);
					end
					processed = true;
				elseif ( IsModifiedClick("CHATLINK") ) then
					local NuN_staticPopup = StaticPopup_Visible("ADD_IGNORE");
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_IGNORE"); end
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_MUTE"); end
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_FRIEND"); end
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_GUILDMEMBER"); end
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_TEAMMEMBER"); end
					if ( not NuN_staticPopup ) then NuN_staticPopup = StaticPopup_Visible("ADD_RAIDMEMBER"); end
					if ( ( not NuN_staticPopup ) and ( not DEFAULT_CHAT_FRAME.editBox:IsVisible() ) and ( locals.NuNDataPlayers[_name] ) ) then
						locals.ttName = _name;
						NuN_ClearPinnedTT();
						NuN_PinnedTooltip:SetOwner(self, "ANCHOR_RIGHT");
						NuN_State.NuN_PinUpHeader = true;
						NuN_PinnedTooltip.type = "Contact";
						NuNSettings[local_player.realmName].pT = {};
						NuNSettings[local_player.realmName].pT.type = "Contact";
						NuNSettings[local_player.realmName].pT.name = locals.ttName;
						NuNF.NuN_BuildTT(NuN_PinnedTooltip);
						NuN_State.NuN_PinUpHeader = false;
						NuN_PinnedTooltip:Show();
					end
				end
			end

		elseif ( IsNuNModifierKeyDown(btn) ) then			-- 5.60
			text = strgsub(text, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\|", ":0:0:0:0:0:0:0|");
			text = strgsub(text, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\124", ":0:0:0:0:0:0:0\124");

			if ( ( NuNGNoteFrame:IsVisible() ) or ( NuNFrame:IsVisible() ) ) then
				if ( NuNGNoteFrame:IsVisible() ) then
					NuNGNoteTextScroll:Insert(text);					-- + v5.00.11200
					processed = true;
				elseif ( NuNFrame:IsVisible() ) then
					NuNText:Insert(text);							-- + v5.00.11200
					processed = true;
				end
				--HideUIPanel(ItemRefTooltip);
				--return true;
			else
				NuNGNoteFrame.fromQuest = nil;
				if ( NuNData[locals.itmIndex_dbKey][text] ) then
					text = ( NuNData[locals.itmIndex_dbKey][text] );
				end
				if ( ( NuNDataRNotes[text] ) or ( NuNDataANotes[text] ) ) then
					local_player.currentNote.general = text;
					NuN_ShowSavedGNote();
					processed = true;
					--HideUIPanel(ItemRefTooltip);					
				else
					ItemRefTooltip:Show();
					if ( not ItemRefTooltip:IsVisible() ) then
						ItemRefTooltip:SetOwner(UIParent, "ANCHOR_PRESERVE");
					end
					ItemRefTooltip:SetHyperlink(link);
					delayedItemTooltip = text;
					processed = true;
				end
				return processed;
			end
		end
	end
	return processed;
end

function NuNNew_PaperDollItemSlotButton_OnModifiedClick(btn)
	if ( NuNSettings[local_player.realmName].modifier == "on" ) then
		local itmLink;

		if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then

		elseif ( IsNuNModifierKeyDown(btn) ) then
			if ( ( InspectFrame ) and ( InspectFrame:IsVisible() ) ) then
				itmLink = GetInventoryItemLink("target", btn:GetID());
			else
				itmLink = GetInventoryItemLink("player", btn:GetID());
			end
			if ( ( itmLink ~= nil ) and ( itmLink ~= "" ) ) then
				itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\|", ":0:0:0:0:0:0:0|");
				itmLink = strgsub(itmLink, "\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\:%-*%d+\124", ":0:0:0:0:0:0:0\124");
				if ( ( NuNGNoteFrame:IsVisible() ) or ( NuNFrame:IsVisible() ) ) then
					if ( NuNGNoteFrame:IsVisible() ) then
						NuNGNoteTextScroll:Insert(itmLink);				-- + v5.00.11200
						return;
					elseif ( NuNFrame:IsVisible() ) then
						NuNText:Insert(itmLink);					-- + v5.00.11200
						return;
					end
				else
					NuNGNoteFrame.fromQuest = nil;
					if ( NuNData[locals.itmIndex_dbKey][itmLink] ) then
						itmLink = ( NuNData[locals.itmIndex_dbKey][itmLink] );
					end
					if ( ( NuNDataRNotes[itmLink] ) or ( NuNDataANotes[itmLink] ) ) then
						local_player.currentNote.general = itmLink;
						NuN_ShowSavedGNote();
					else
						NuNF.NuN_GNoteFromItem(itmLink, "GameTooltip");
					end
					return;
				end
			end
		end
	end
end

--[[
Post-hook for secure function SetAbandonQuest().  Called when the user abandons a quest (erm, duh?); hooked to allow us to stop
tracking this quest for the quest history module.
--]]
function NuNNew_SetAbandonQuest()
	NuN_State.NuN_abandonQuest = GetAbandonQuestName();
end

-- 5.60 Removed the Quest Frame Item on click note creation hooks   2.2 nerfed them, and they weren't missed so didn't try to find out what the new function names would be....
--QuestLogFrameAbandonButton
function NuNNew_AbandonQuest()
	if ( NuN_State.NuN_abandonQuest ) then
		locals.timeSinceLastUpdate = 0;
		NuN_State.NuN_IgnoreNextQUpdate = true;
		if ( NuNData[local_player.realmName].QuestHistory[locals.player_Name][NuN_State.NuN_abandonQuest] ) then
			NuNData[local_player.realmName].QuestHistory[locals.player_Name][NuN_State.NuN_abandonQuest].abandoned = true;
			if ( locals.NuNQuestLog[NuN_State.NuN_abandonQuest] ) then
				locals.NuNQuestLog[NuN_State.NuN_abandonQuest] = nil;
			end
		end
	end
end

function NuNNew_QuestDetailAcceptButton_OnClick()
	NuN_QuestAccepted = GetTitleText();
	if ( ( NuNSettings[local_player.realmName].autoMapNotes ) and ( NuNSettings[local_player.realmName].autoQ ) ) then
		NuN_MapNote("Target", NUN_QUEST_GIVER, "", 9);
	end
end

-- THIS is the important function for monitoring when Quests are handed in
function NuNNew_QuestRewardCompleteButton_OnClick()
	local q = GetTitleText();
	local i = 1;
	local f = 0;

	if ( NuNData[local_player.realmName].QuestHistory[locals.player_Name][q] ) then
		local l_c_note = local_player.currentNote.general;
		local l_c_name = local_player.currentNote.unit;
		local qChar = NuN_CheckTarget();
		if ( qChar == "N" ) then
			qChar = local_player.currentNote.general;
		else
			qChar = "";
		end
		
		--@fixme orgevo: sigh....what the fuck IS all this code doing?
		NuNData[local_player.realmName].QuestHistory[locals.player_Name][q].handedIn = 1;
		for i = 1, 4, 1 do
			f = strfind(NuNData[local_player.realmName].QuestHistory[locals.player_Name][q].txt, NuNC.NUN_FINISHED, (f+1) );
			if ( f == nil ) then
				f = i;
				break;
			end
		end
		if ( f < 4 ) then
			local qTxt = NuNF.NuN_GetDisplayText( NuNData[local_player.realmName].QuestHistory[locals.player_Name][q].txt );
			qTxt = qTxt.."\n\n".. NuNC.NUN_FINISHED .."   "..qChar.."\n    "..NuNF.NuN_GetDateStamp().."\n    "..NuNF.NuN_GetLoc().."\n";
			NuNData[local_player.realmName].QuestHistory[locals.player_Name][q].txt = NuNF.NuN_SetSaveText(qTxt);
		end
		local_player.currentNote.general = l_c_note;
		local_player.currentNote.unit = l_c_name;
	end
end

function NuNNew_ChatFrame_MessageEventHandler(chatframe, event, ...)
	local processedByNuN = nil;
	local localDbg = "";
	local arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15 = ...;
	-- hook all necessary AddMessage functions for each chat frame
	if ( locals.processAddMessage ) then
		if ( not chatframe.NuNOri_AddMessage ) then
			chatframe.NuNOri_AddMessage = chatframe.AddMessage;
			chatframe.AddMessage = NuN_Statics.NuNNew_AddMessage;
		end
	elseif ( chatframe.NuNOri_AddMessage ) then
		chatframe.AddMessage = chatframe.NuNOri_AddMessage;
		chatframe.NuNOri_AddMessage = nil;
	end

	--[[
		arg1: text of the message
		arg2: sender's name
		arg3: language (empty if english, I guess)
		arg4: zone
		arg5: target's name
		arg6: chat flags (AFK, DND, etc.)
		arg7: zoneID for system channels (1 for General, 2 for Trade, 22 for LocalDefense, 23 for WorldDefense, 26 for LFG)
		arg8: channel number
		arg9: channel name without number
		arg10: 
		arg11: chat lineID (used for reporting messages)
		arg12: sender GUID
		arg13: 
		arg14: 
		arg15:
	--]]

	if locals.NuNDebugVerbose then
		if arg1 and chatframe == DEFAULT_CHAT_FRAME then
			nun_msgf("NuNNew_ChatFrame_MessageEventHandler - event:%s  frame:%s  arg2:%s  arg3:%s  arg4:%s  arg5:%s  arg6:%s  arg7:%s  arg8:%s  arg9:%s  arg10:%s  arg11:%s  arg12:%s  arg13:%s  arg14:%s  arg15:%s   arg1:%s",
				tostring(event), tostring(chatframe), tostring(arg2), tostring(arg3), tostring(arg4), tostring(arg5), tostring(arg6), tostring(arg7), tostring(arg8),
				tostring(arg9), tostring(arg10), tostring(arg11), tostring(arg12), tostring(arg13), tostring(arg14), tostring(arg15), tostring(arg1));
		else
			nun_msgf("NuNNew_ChatFrame_MessageEventHandler - event:%s  args:NONE", tostring(event));
		end
	end

--@todo orgevo - add this optimization 
--if ( strsub(event, 1, 8) == "CHAT_MSG" ) then

	-- Suppress Friend not found messages when attempting to Befriend someone
	if ( ( event == "CHAT_MSG_SYSTEM" ) and ( arg1 == ERR_FRIEND_NOT_FOUND ) and ( NuN_AttemptedFriendIgnores > 0 ) ) then
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores - 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			local playerNameTxt = "";
			if locals.nameLastAttemptedFriendUpdate then
				--@todo orgevo - the intent here is to show which friend no longer exists, so the player can remove them from the list, but the AddFriend() functionality
				-- is an asynchronous procedure, so I need to spend a little more time on this to implement it correctly
				--playerNameTxt = " (" .. locals.nameLastAttemptedFriendUpdate .. ")";
			end
			NuN_Message(ERR_FRIEND_NOT_FOUND .. playerNameTxt);
		end
		processedByNuN = true;

	-- Suppress Ignore not found messages when attempting to Ignore someone
	elseif ( ( event == "CHAT_MSG_SYSTEM" ) and ( arg1 == ERR_IGNORE_NOT_FOUND ) and ( NuN_AttemptedFriendIgnores > 0 ) ) then
		NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores - 1;
		if ( not NuNSettings[local_player.realmName].autoS ) then
			local playerNameTxt = "";
			if locals.nameLastAttemptedIgnoreUpdate then
				--@todo orgevo - the intent here is to show which friend no longer exists, so the player can remove them from the list, but the AddFriend() functionality
				-- is an asynchronous procedure, so I need to spend a little more time on this to implement it correctly
				--playerNameTxt = " (" .. locals.nameLastAttemptedIgnoreUpdate .. ")";
			end
			NuN_Message(ERR_IGNORE_NOT_FOUND .. playerNameTxt);
		end
		processedByNuN = true;

	-- if sending mutliple lines of a message to someone who is AFK or DND, then we don't want to receive MULTIPLE "X is AFK" messages echoed back to us - we only need to see 1 for each set of lines sent
	elseif ( ( ( event == "CHAT_MSG_AFK" ) or ( event == "CHAT_MSG_DND" ) ) and ( arg2 ) and ( arg2 == busySending.user ) ) then
		if ( busySending.counter > 0 ) then
			processedByNuN = true;
		end
		busySending.counter = 1;

	-- if waitng for a /who, then ignore Chat_msg_system messages that contain the name of the /who we were looking for...
	-- won't ignore more than 1 but shouldn't usually have > 1, and its only a chat message anyway so...
	elseif ( event == "CHAT_MSG_SYSTEM" ) then

		if ( type(arg1) == "string" ) then
			if ( ( NuN_WhoReturnStruct.func ) and ( strfind(arg1, NuN_WhoReturnStruct.name) ) ) then
				NuN_WhoReturnStruct.func();														-- 5.60
				NuN_WhoReturnStruct.func = nil;													-- 5.60
				NuN_WhoReturnStruct.name = nil;													-- 5.60
				NuN_WhoReturnStruct.timeLimit = nil;											-- 5.60
				NuN_WhoReturnStruct.secondTry = nil;
				if ( ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].alternativewho ) ) then
					SetWhoToUI(0);                                          					-- 5.60
					FriendsFrame:RegisterEvent("WHO_LIST_UPDATE");
				end
				processedByNuN = true;
				NuN_suppressExtraWho = true;

			elseif ( ( NuN_suppressExtraWho ) 
					and	 ( ( strfind(arg1, WHO_NUM_RESULTS) )
							or ( ( WHO_NUM_RESULTS_P1 ) and( strfind(arg1, WHO_NUM_RESULTS_P1) ) ) ) ) then
				processedByNuN = true;
				NuN_suppressExtraWho = nil;
			end
		end
			
	-- This is the bulk of the message parsing required to reconstruct notes sent from other players
	-- Multiple lines are needed to send NuN notes, and these lines are connected by a _msgKey which identifies them as a set
	-- NOTE 1 :	Only "Formatted" NotesUNeed transmissions contain the information examined below to allow note reconstruction
	--		Unformatted note transmissions, are just received as a normal sequence of chat messages
	-- NOTE 2 :	We ignore messages with a key that we have just sent to avoid receiving our own transmissions
	-- locals.NuN_Receiving is an AddOn Global array variable holding all the relevant information about a Note being received from another user
	-- 5.60 THIS LIST OF TESTS BELOW MAY NEED ADDING TO GIVEN THE EXTRA SEND OPTIONS I TRIED TO INCLUDE...
--	elseif ( ( event == "CHAT_MSG_WHISPER" ) or 
--			 ( event == "CHAT_MSG_GUILD" ) or 
--			 ( event == "CHAT_MSG_PARTY" ) or 
--			 ( event == "CHAT_MSG_RAID" ) or 
--			 ( event == "CHAT_MSG_RAID_WARNING" ) or				-- 5.60
--			 ( event == "CHAT_MSG_SAY" ) or 						-- 5.60
--			 ( event == "CHAT_MSG_OFFICER" ) or					-- 5.60
--			 ( event == "CHAT_MSG_BATTLEGROUND" ) ) then			-- 5.60
	elseif ( type(arg1) == "string" ) then
		-- I think this suppresses echoing our own formatted messages as we send them
		if ( NuN_msgKey ) then
			local kLen = strlen(NuN_msgKey);
			local kTst = strsub(arg1, 1, kLen);
			if ( kTst == NuN_msgKey ) then
				return true;
			end
		end

		-- waiting for decision to receive a previous note which would have replaced an existing note
		if ( receiptPending ) then
--			processedByNuN = true;				-- ???  Stop processing OK, but why flag as processed and hide the message ???

		-- if in the middle of receiving a series of messages that complete a note, then parse it based on its type
		-- i.e. different line types hold different information, such as basic text, or Drop Down box contents, etc.,  Or control messages such as First message in a sequence / Final message in a sequence
		-- NOTE 1 :	Note transmission depends on ordered, sequential FIFO transmission of messages (i..e they will be received in the same sequence that they were sent for easy re-building of the original note)
		-- NOTE 2 :	NuN triggers a Timer when the first message in a sequence arrives, which is then monitored in OnUpdate.
		--		If a "Note Ends" type message (Line Type 4) isn't received within a certain amount of time, then the Note Receiving process is abandoned, and the user warned
		elseif ( locals.NuN_Receiving.active ) then
			local lLineType = 0;
			if ( strfind(arg1, locals.NuN_TransStrings[2][1]) ) then
				lLineType = 2;
			elseif ( strfind(arg1, locals.NuN_TransStrings[2][2]) ) then
				lLineType = 2;
			elseif ( strfind(arg1, NuNC.NUN_SOURCE) ) then
				lLineType = 3;
			elseif ( strfind(arg1, locals.NuN_TransStrings[3][1]) ) then
				lLineType = 4;
			elseif ( strfind(arg1, locals.NuN_TransStrings[3][2]) ) then
				lLineType = 4;
			end
		
			-- LineType 4 : Signifies Last Message in sequence, and the note can now be created
			if ( ( lLineType == 4 ) and ( locals.NuN_Receiving.version ) ) then
				locals.NuN_Receiving.log = locals.NuN_Receiving.log.."\n"..arg1;			-- NuN Creates a NuN Log type note recording the last 10 transmitted/received notes for tracking / debugging purposes
				NuN_CreateReceivedNote();
				if ( not receiptPending ) then
					locals.NuN_Receiving = {};
					NuN_uCount = 999;
					NuN_tCount = 999;
					receiptDeadline = defaultReceiptDeadline;
				end
				processedByNuN = true;
				locals.NuN_Receiving.timer = 0;

			-- LineType 2 : identifies whether the note being sent is for a Contact or a General Note, and the Name under which the note should be saved
			elseif ( ( lLineType == 2 ) and ( locals.NuN_Receiving.version ) ) then
				locals.NuN_Receiving.log = locals.NuN_Receiving.log.."\n"..arg1;			-- NuN Creates a NuN Log type note recording the last 10 transmitted/received notes for tracking / debugging purposes
				local nameStart = strfind(arg1, ": Contact : ");
				if ( nameStart ) then
					locals.NuN_Receiving.type = "Contact";
					nameStart = nameStart + 12;
					locals.NuN_Receiving.title = strsub(arg1, nameStart);
					processedByNuN = true;
					locals.NuN_Receiving.timer = 0;
				else
					nameStart = strfind(arg1, ": General");
					if ( nameStart ) then
						locals.NuN_Receiving.type = "General";
						locals.NuN_Receiving.subtype = tonumber( strsub(arg1, nameStart+10, nameStart+10) );
						nameStart = nameStart + 15;
						locals.NuN_Receiving.title = strsub(arg1, nameStart);
						processedByNuN = true;
						locals.NuN_Receiving.timer = 0;
					end
				end

			-- LineType 3 : Control message specifying the client language of the Sender, and the version of NotesUNeed they use
			-- Transmission of notes between different versions of NuN needed to be handled
			elseif ( lLineType == 3 ) then
				locals.NuN_Receiving.log = locals.NuN_Receiving.log.."\n"..arg1;			-- NuN Creates a NuN Log type note recording the last 10 transmitted/received notes for tracking / debugging purposes
				local split = strfind(arg1, "v");
				locals.NuN_Receiving.lang = strsub(arg1, NuNC.NUN_SOURCE_LEN+locals.NuN_Receiving.pos+1, split-2);
				locals.NuN_Receiving.version = strsub(arg1, split+1);
				processedByNuN = true;
				locals.NuN_Receiving.timer = 0;

			elseif  ( locals.NuN_Receiving.version ) then
			-- The MAIN Body of the message is flagged with a message key prefix to identify chat messages that need to be included in the Note. They are not assigned a 'global' line type in the above sequence
			-- Line Type below is determined by a Text flag in the message at locals.NuN_Receiving.pos + 1 ;p
				local msgPrefix = strsub(arg1, 1, locals.NuN_Receiving.pos);
				if ( msgPrefix == locals.NuN_Receiving.prefix ) then
					locals.NuN_Receiving.log = locals.NuN_Receiving.log.."\n"..arg1;			-- NuN Creates a NuN Log type note recording the last 10 transmitted/received notes for tracking / debugging purposes
					local lineType = strsub(arg1, locals.NuN_Receiving.pos+1, locals.NuN_Receiving.pos+1);
					local lineData = strsub(arg1, locals.NuN_Receiving.pos+3);

					-- LineType "G" : Information from the Drop Down boxes in a Contact Note
					if ( lineType == "G" ) then
						if ( strfind(arg1, "Horde" ) ) then
							locals.NuN_Receiving.faction = "Horde";
						else
							locals.NuN_Receiving.faction = "Alliance";
						end
						local tBeg = strfind(arg1, "<");
						local tEnd = strfind(arg1, ">");
						if ( ( tBeg ) and ( tEnd ) ) then
							value = "0";
							local nextOne = strfind(arg1, ",", tBeg);
							local value = strsub(arg1, tBeg+1, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.race = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.class = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.sex = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.prating = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.prof1 = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.prof2 = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, nextOne-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.arena = tonumber(value);
							end
							value = "0";
							tBeg = nextOne+1;
							nextOne = strfind(arg1, ",", tBeg);
							value = strsub(arg1, tBeg, tEnd-1);
							if ( value > "0" ) then
								locals.NuN_Receiving.hrank = tonumber(value);
							end
						end

					-- Line Type "U" : Information from the User Definable Buttons in a Contact Note
					elseif ( lineType == "U" ) then
						local usrData = tonumber( strsub(lineData, 1, 1) );
						if ( usrData ~= (NuN_uCount+1) ) then
							return;
						end
						NuN_uCount = NuN_uCount + 1;
						if ( NuN_uCount > 5 ) then
							return;
						end
						if ( NuN_uCount == 1 ) then
							locals.NuN_Receiving.user = {};
						end
						local titleEnd = strfind(lineData, "~    ");
						local detlBeg = strfind(lineData, "~    ~");
						if ( titleEnd ) then
							locals.NuN_Receiving.user[NuN_uCount] = {};
							locals.NuN_Receiving.user[NuN_uCount].title = strsub(lineData, 3, titleEnd-1);
						end
						if ( detlBeg ) then
							detlBeg = detlBeg + 6;
							if ( locals.NuN_Receiving.user[NuN_uCount] ) then
								locals.NuN_Receiving.user[NuN_uCount].detl = strsub(lineData, detlBeg);
							end
						end

					-- Line Type "T" : Text information from the main Text Edit box in either a General OR a Contact Note
					elseif ( lineType == "T" ) then
						if ( lastTextKey == strsub(lineData, 1, 1) ) then
							return;
						else
							lastTextKey = strsub(lineData, 1, 1);
						end
						NuN_tCount = NuN_tCount + 1;
						if ( NuN_tCount == 1 ) then
							locals.NuN_Receiving.text = {};
						end
						locals.NuN_Receiving.text[NuN_tCount] = strsub(lineData, 2);
					end
					processedByNuN = true;
					locals.NuN_Receiving.timer = 0;
				end
			end
		else
			-- Different language clients have different localised headings, and this was needed to translate them
			local prfx1 = "";
			if ( strfind(arg1, locals.NuN_TransStrings[1][1]) ) then
				prfx1 = locals.NuN_TransStrings[1][1];
			elseif ( strfind(arg1, locals.NuN_TransStrings[1][2]) ) then
				prfx1 = locals.NuN_TransStrings[1][2];
			end

			-- if the message is the first in a series indicating a NuN Note is being sent to you, then locals.NuN_Receiving.active = true
			-- i.e. if the LineType is a NotesUNeed sent message Prefix then........
			local prfxPos = strfind(arg1, prfx1);
			if ( ( prfx1 ~= "" ) and ( prfxPos ) ) then
				local prfxLen = strlen(prfx1);
				local prfxEnd = strfind(arg1, "  --->");
				locals.NuN_Receiving.from = strsub(arg1, prfxPos+prfxLen, prfxEnd-1);
				locals.NuN_Receiving.pos = strfind(arg1, ":", 1);
				if ( locals.NuN_Receiving.pos ) then
					local __, __, delay = strfind(arg1, ".+::(%d+%.*%d*)::.*");				-- Custom time delay between each line
					if ( delay ) then delay = tonumber(delay); end
					if ( ( delay ) and ( delay > defaultReceiptDeadline ) ) then
						receiptDeadline = delay;
					else
						receiptDeadline = defaultReceiptDeadline;
					end
					locals.NuN_Receiving.prefix = strsub(arg1, 1, locals.NuN_Receiving.pos);
					locals.NuN_Receiving.active = true;
					locals.NuN_Receiving.timer = 0;
					NuN_uCount = 0;
					NuN_tCount = 0;
					processedByNuN = true;
					NuN_Message(NUN_RECEIVING_START..NuN_Receiving.from);
					locals.NuN_Receiving.log = arg1;			-- NuN Creates a NuN Log type note recording the last 10 transmitted/received notes for tracking / debugging purposes
					locals.NuN_Receiving.receivedKeys = {};
					lastTextKey = "";
				end
			end
		end
	end

	-- don't echo messages that have been intercepted and processed by NuN
	if ( processedByNuN ) then
		if ( ( not NuN_State.NuN_AtStartup ) and ( locals.NuN_Receiving.active ) ) then
			PlaySound("TellMessage");		-- beep to let the user know that we're still receiving a note from someone else
		end
		return true;
	else
--[[
		if arg1 and arg12 and arg12 ~= "" then
			arg2 = NotesUNeed.NuN_Statics.TagPlayerChatName(arg2, chatframe == DEFAULT_CHAT_FRAME);
			if locals.NuNDebug and chatframe == DEFAULT_CHAT_FRAME then
				nun_msgf("!!seeing msg from player!!   returning playername value of '%s'", strgsub(arg2, "\124", "\124\124"));
			end
			return false, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15;
		end
--]]
		if locals.NuNDebug then
			locals.showColoredNameDebug = true;
		end
		return false;
	end
end

function NuN_RegisterChatFilter()
	ChatFrame_AddMessageEventFilter("FRIENDLIST_UPDATE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("IGNORELIST_UPDATE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("MUTELIST_UPDATE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHANNEL_INVITE_REQUEST", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_ACHIEVEMENT", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_ADDON", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_AFK", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BATTLEGROUND", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BATTLEGROUND_LEADER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BG_SYSTEM_ALLIANCE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BG_SYSTEM_HORDE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BG_SYSTEM_NEUTRAL", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_CONVERSATION", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_CONVERSATION_LIST", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_CONVERSATION_NOTICE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_INLINE_TOAST_ALERT", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_INLINE_TOAST_BROADCAST", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_INLINE_TOAST_BROADCAST_INFORM", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_INLINE_TOAST_CONVERSATION", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_WHISPER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_BN_WHISPER_INFORM", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL_JOIN", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL_LEAVE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL_LIST", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL_NOTICE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_CHANNEL_NOTICE_USER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_DND", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_EMOTE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_FILTERED", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_GUILD", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_ACHIEVEMENT", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_MSG_IGNORED", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_MONSTER_EMOTE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_MONSTER_SAY", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_MONSTER_WHISPER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_MONSTER_YELL", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_OFFICER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_OPENING", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_PARTY", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_PARTY_LEADER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_PET_INFO", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_RAID", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_RAID_BOSS_EMOTE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_RAID_BOSS_WHISPER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_RAID_WARNING", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_RAID_LEADER", NuNNew_ChatFrame_MessageEventHandler);
--	ChatFrame_AddMessageEventFilter("CHAT_MSG_RESTRICTED", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_SAY", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_SYSTEM", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_TARGETICONS", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_TRADESKILLS", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_TEXT_EMOTE", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_WHISPER", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_WHISPER_INFORM", NuNNew_ChatFrame_MessageEventHandler);
	ChatFrame_AddMessageEventFilter("CHAT_MSG_YELL", NuNNew_ChatFrame_MessageEventHandler);
--	ChatFrame_AddMessageEventFilter("CHAT_MSG_", NuNNew_ChatFrame_MessageEventHandler);
--	ChatFrame_AddMessageEventFilter("CHAT_MSG_", NuNNew_ChatFrame_MessageEventHandler);
--	ChatFrame_AddMessageEventFilter("CHAT_MSG_", NuNNew_ChatFrame_MessageEventHandler);
--	ChatFrame_AddMessageEventFilter("CHAT_MSG_", NuNNew_ChatFrame_MessageEventHandler);
end


-- World Map Frame was altered to allow normal GameTooltips to appear above it
-- !!! again this is code that would be better replaced with a proper world map tooltip, but I don't have the testing ability
-- btw, the reason I do this is because I want the NuN_MapTooltip to be shown over MapNotes in AlphaMap and Atlas, as well as the WorldMap
-- and letting it be a "child" of the World Map means its not visible over alphaMap / atlas.... so I make it a normal tooltip...
-- could test trying to change the parent, and see what happens, other addons that change the world map anyway can do so and not affect me I think.
-- Worth a try ....
function NuNNew_ToggleWorldMap(frame)
	if ( frame == WorldMapFrame ) then
		if ( ( UIPanelWindows["WorldMapFrame"] ) and ( UIPanelWindows["WorldMapFrame"].area == "full" ) ) then
			UIPanelWindows["WorldMapFrame"] = { area="center", pushable=UIPanelWindows["WorldMapFrame"].pushable };
		end
		NuNPopup:Hide();
		NuN_MapTooltip:Hide();
		if ( WorldMapFrame:IsVisible() ) then
			NuN_UpdateCurLinks();
		end
	end
end

function NuN_UpdateCurLinks()
	local cont, zone, curZ;

	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		cont = "WM ";								-- + v5.00.11200
		local map = GetMapInfo();						-- + v5.00.11200
		if ( map ) then								-- + v5.00.11200
			cont = cont..map;						-- + v5.00.11200
		else									-- + v5.00.11200
			cont = cont.."WorldMap";					-- + v5.00.11200
		end									-- + v5.00.11200
		zone = 0;								-- + v5.00.11200
		if ( MapNotes_Data_Notes[cont] ) then					-- + v5.00.11200
			NuN_IndexByZone(cont, zone, MapNotes_Data_Notes[cont]);		-- + v5.00.11200
		end									-- + v5.00.11200
	end										-- + v5.00.11200
end

function NuNNew_AlphaMapNotes_OnEnter(id, lFrame)
	if ( ( NuNPopup:IsVisible() ) and ( IsControlKeyDown() ) ) then
		return;	-- suppress tooltip changes while the Control key is pressed
	end
	NuNHooks.NuNOri_AlphaMapNotes_OnEnter(id, lFrame);
	NuN_WorldMapTooltip_OnShow(id, GameTooltip);
end

-------------------------------------------------------------------------------------------
-- Attempt to Hook MapNotes functions

function NuNNew_MapNotes_OnEnter(id)
	if ( ( NuNPopup:IsVisible() ) and ( IsControlKeyDown() ) ) then
		return;	-- suppress tooltip changes while Control Key is pressed
	end
	NuNHooks.NuNOri_MapNotes_OnEnter(id);
	NuN_WorldMapTooltip_OnShow(id, GameTooltip);
end

function NuNNew_MapNotes_OnLeave(id)
--	if ( NuNPopup:IsVisible() ) then
--		locals.popUpHide = true;
--		NuNPopup.id = id;
--		return;
--	else
		locals.popUpHide = true;
		NuNHooks.NuNOri_MapNotes_OnLeave(id);
--	end
end

function NuNNew_MapNotes_DeleteNote(self, btn, id, cont, zone)
	local lId = id;
	local lCont, lZone, lLst;

	id = tonumber(id);

	if ( id > 0 ) then
		lCont, lZone, lLst = NuN_PreDeleteMapIndex(id, cont, zone);
	end
	NuNHooks.NuNOri_MapNotes_DeleteNote(id, cont, zone);
	if ( ( lId > 0 ) and ( lCont ) and ( lCont ~= 0 ) ) then
		NuN_DeleteMapIndex(lId, lCont, lZone, lLst);
	end
end

function NuNNew_MapNotes_WriteNote()
	NuNHooks.NuNOri_MapNotes_WriteNote();
	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		local cont = "WM ";							-- + v5.00.11200
		local map = GetMapInfo();						-- + v5.00.11200
		if ( map ) then								-- + v5.00.11200
			cont = cont..map;						-- + v5.00.11200
		else									-- + v5.00.11200
			cont = cont.."WorldMap";					-- + v5.00.11200
		end									-- + v5.00.11200
		zone = 0;								-- + v5.00.11200
		if ( MapNotes_Data_Notes[cont] ) then					-- + v5.00.11200
			NuN_IndexByZone(cont, zone, MapNotes_Data_Notes[cont]);		-- + v5.00.11200
		end									-- + v5.00.11200
	end										-- + v5.00.11200
end

function NuNNew_MapNotes_Quicknote()
	NuNHooks.NuNOri_MapNotes_Quicknote();
	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		local cont = "WM ";							-- + v5.00.11200
		local map = GetMapInfo();						-- + v5.00.11200
		if ( map ) then								-- + v5.00.11200
			cont = cont..map;						-- + v5.00.11200
		else									-- + v5.00.11200
			cont = cont.."WorldMap";					-- + v5.00.11200
		end									-- + v5.00.11200
		zone = 0;								-- + v5.00.11200
		if ( MapNotes_Data_Notes[cont] ) then					-- + v5.00.11200
			NuN_IndexByZone(cont, zone, MapNotes_Data_Notes[cont]);		-- + v5.00.11200
		end									-- + v5.00.11200
	end										-- + v5.00.11200
end


----------------------------
-- No More Hookers --
------------------------------------------------------------------------------------------------------------------------------------------------------------------------------





function NuN_Who()
	if ( not NuN_WhoReturnStruct.func ) then												-- 5.60
	    -- make sure the /who call triggers an EVENT, rather than a chat message 										-- 5.60
		if ( ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].alternativewho ) ) then
			SetWhoToUI(1);                                          						-- 5.60
			FriendsFrame:UnregisterEvent("WHO_LIST_UPDATE");								-- 5.60
		end
		-- unregister the who frame from the who update events, and trigger the who									-- 5.60
		-- also set up the Who Return Structure so the EVENT returns to the correct function								-- 5.60
		NuN_WhoReturnStruct.func = NuN_Who;													-- 5.60
		NuN_WhoReturnStruct.timeLimit = 0;													-- 5.60
		NuN_WhoReturnStruct.name = local_player.currentNote.unit;													-- 5.60
		NuN_WhoReturnStruct.secondTry = nil;
		NuN_suppressExtraWho = true;
		SendWho("n-"..local_player.currentNote.unit);																-- 5.60 "n-"..

	elseif ( NuN_WhoReturnStruct.func == NuN_Who ) then										-- 5.60
		local wName = nil;
		local wGuildName = nil;
		local wRace = nil;
		local wClass = nil;
		local wLevel = nil;
		local bttnHeadingText1;
		local bttnDetailText1;
		local wZone = nil;
	
		local n = GetNumWhoResults();
		for i = 1, n, 1 do
			wName, wGuildName, wLevel, wRace, wClass, wZone = GetWhoInfo(i);							-- 5.60 merged double call
			if ( wName == local_player.currentNote.unit ) then														-- Not interested in Level / Zone
				if ( wGuildName ~= nil ) then
					contact.guild = wGuildName;
				end
				bttnHeadingText1 = _G["NuNTitleButton1ButtonTextHeading"];
				bttnDetailText1 = _G["NuNInforButton1ButtonTextDetail"];
				if ( bttnHeadingText1:GetText() == NUN_DFLTHEADINGS[1] ) and ( wGuildName ~= nil) then
					bttnDetailText1:SetText(wGuildName);
					contact.guild = wGuildName;
					if ( wGuildName == "" ) then
						locals.bttnChanges[6] = -1;												-- 5.60 Use -1 to flag blank
					else
						locals.bttnChanges[6] = wGuildName;
					end
				end
				if ( wClass ~= nil ) then
					contact.class = wClass;
					locals.dropdownFrames.ddClass = NuNF.NuNGet_TableID(locals.Classes, contact.class);
					UIDropDownMenu_SetSelectedID(locals.NuNClassDropDown, locals.dropdownFrames.ddClass);
					UIDropDownMenu_SetText(locals.NuNClassDropDown, contact.class);
				end
				if ( wRace ~= nil ) then
					contact.race = wRace;
					locals.dropdownFrames.ddRace = NuNF.NuNGet_TableID(locals.Races, contact.race);
					UIDropDownMenu_SetSelectedID(locals.NuNRaceDropDown, locals.dropdownFrames.ddRace);
					UIDropDownMenu_SetText(locals.NuNRaceDropDown, contact.race);
				end

				-- auto update existing notes - but not saving new notes automatically
				if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
					NuN_WriteNote();
				end

			end
		end
	end																						-- 5.60
end

-- 5.60 Replaces NuN_Target() call from XML
function NuN_UnitInfo_OnClick()
	local unitID = NuNF.NuN_UnitInfo(true, local_player.currentNote.unit);
	if ( unitID ) then
		NuNF.NuN_UnitInfo(false, local_player.currentNote.unit, unitID);	-- UnitID ? Bleh ! Fixed - but untested fix
	end
end



-- The Main Contact Note Deletion Routine
function NuN_Delete(noRefresh)
	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
		locals.NuNDataPlayers[local_player.currentNote.unit] = nil;
	end

	for n = 1, locals.uBttns, 1 do
		locals.headingName = local_player.currentNote.unit .. locals.pHead .. n;
		locals.headingDate = local_player.currentNote.unit..locals.pDetl..n;
		if ( locals.NuNDataPlayers[locals.headingName] ) then
			locals.NuNDataPlayers[locals.headingName] = nil;
		end
		if ( locals.NuNDataPlayers[locals.headingDate] ) then
			locals.NuNDataPlayers[locals.headingDate] = nil;
		end
	end
	if ( NuN_CTTCheckBox:GetChecked() ) then
		NuN_ClearPinnedTT();
	end

	if ( noRefresh ) then
		return;
	end

	NuNEditDetailsFrame:Hide();
	HideNUNFrame();

	if ( ( NuNSearchFrame:IsVisible() ) and ( not strfind(NuNSearchTitleText:GetText(), NUN_QUESTS_TEXT) ) ) then
		locals.deletedE = true;
		NuNSearch_Search();
	end
	if ( FriendsListFrame:IsVisible() ) then
		NuNNew_FriendsList_Update();
	elseif ( IgnoreListFrame:IsVisible() ) then
		NuNNew_IgnoreList_Update();
	elseif ( WhoFrame:IsVisible() ) then
		NuNNew_WhoList_Update();
	end
	
	
	-- must do this seperately from the others, since it's now in its own frame
	if ( GuildFrame and GuildFrame:IsVisible() ) then
		if ( GuildRosterFrame and GuildRosterFrame:IsVisible() ) then
			NuNNew_GuildStatus_Update();
			
		-- other guild frame tabs...

		end
	end
end

-- The Main General Note Deletion Routine
function NuNGNote_Delete(noRefresh)
	local c_note = NuNGNoteTitleButtonText:GetText();
	if ( NuNGNoteFrame.fromQuest ) then
		NuNQuestHistory[c_note] = nil;
		NuNGNoteFrame:Hide();
		if ( ( NuNSearchFrame:IsVisible() ) and ( not noRefresh ) ) then
			locals.deletedE = true;
			NuN_FetchQuestHistory();
		end
	else
		if ( ( strfind(c_note, "|Hitem:") ) and ( not noRefresh ) ) then
			for idx in pairs(NuNData[locals.itmIndex_dbKey]) do
				if ( NuNData[locals.itmIndex_dbKey][idx] == toDelete ) then
					NuNData[locals.itmIndex_dbKey][idx] = nil;
				end
			end
		end
		if ( NuNDataRNotes[c_note] ) then
			NuNDataRNotes[c_note] = nil;
		elseif ( NuNDataANotes[c_note] ) then
			NuNDataANotes[c_note] = nil;
		end
		if ( NuN_GTTCheckBox:GetChecked() ) then
			NuN_ClearPinnedTT();
		end

		if ( noRefresh ) then
			return;
		end

		NuNGNoteFrame:Hide();
		if ( NuNSearchFrame:IsVisible() ) then
			locals.deletedE = true;
			NuNSearch_Search();
		end
		if ( ( QuestLogFrame:IsVisible() ) and ( not NuNGNoteFrame.fromQuest ) ) then
			QuestLog_Update();
		end
		NuN_UpdateMapNotesIndex(c_note);
	end
end



function NuNOptions_ResetDefaults()
	NuNSettings[local_player.realmName] = {};
	NuNSettings[local_player.realmName].autoG = nil;
	NuNSettings[local_player.realmName].autoA = nil;
	NuNSettings[local_player.realmName].autoFI = nil;
	NuNSettings[local_player.realmName].autoD = nil;
	NuNSettings[local_player.realmName].hignores = nil;
	NuNSettings[local_player.realmName].toolTips = "1";
	NuNSettings[local_player.realmName].pScale = 1.00;
	NuNSettings[local_player.realmName].tScale = 1.00;
	NuNSettings[local_player.realmName].mScale = 1.00;
	NuN_PinnedTooltip:SetScale(1);
	NuN_Tooltip:SetScale(1);
	WorldMapTooltip:SetScale(1);
	NuN_MapTooltip:SetScale(1);
	NuNPopup:SetScale(1);
	NuNSettings[local_player.realmName].dLevel = "1";
	NuNSettings[local_player.realmName].autoQ = nil;
	NuNSettings[local_player.realmName].autoN = nil;
	NuNSettings[local_player.realmName].autoP = nil;
	NuNSettings[local_player.realmName].minOver = nil;
	NuNSettings[local_player.realmName].ttLen = NuNC.NUN_TT_MAX;
	NuNSettings[local_player.realmName].ttLLen = NuNC.NUN_TT_LEN;
	NuNSettings[local_player.realmName].hideMicro = nil;
	NuNSettings[local_player.realmName].autoGuildNotes = nil;									-- 5.60
	NuNSettings[local_player.realmName].autoGRVerbose = nil;									-- 5.60
	NuNSettings[local_player.realmName].modKeys = nil;										-- 5.60
	NuNSettings[local_player.realmName].antiKey = nil;										-- 5.60
	NuNSettings[local_player.realmName].mouseBttn = nil;										-- 5.60
	NuNOptions_SetModifierText();											-- 5.60
	NuNSettings[local_player.realmName].modifier = "on";										-- 5.60
	NuNSettings[local_player.realmName].delay = nil;											-- 5.60 Effect won't be noticed until relog
	NuNSettings[local_player.realmName].restrictwho = nil;
	NuNSettings[local_player.realmName].alternativewho = nil;
	NuNSettings[local_player.realmName].lastNote = {};
	-- Reset colour picker presets
	local cpKey, bttn, r, g, b;
	for i=1, 5, 1 do
		-- Contact note frame button
		cpKey = "cc"..i;
		NuNSettings[local_player.realmName][cpKey] = NuNC.NUN_CPRESETS[i];
		bttn = _G["NuNCColourPreset"..i];
		bttn.preset = NuNSettings[local_player.realmName][cpKey];
		r, g, b = NuNF.NuN_HtoD(bttn.preset);
		_G[bttn:GetName().."Texture"]:SetVertexColor(r, g, b);
		-- General note frame button
		cpKey = "gc"..i;
		NuNSettings[local_player.realmName][cpKey] = NuNC.NUN_CPRESETS[i];
		bttn = _G["NuNGColourPreset"..i];
		bttn.preset = NuNSettings[local_player.realmName][cpKey];
		r, g, b = NuNF.NuN_HtoD(bttn.preset);
		_G[bttn:GetName().."Texture"]:SetVertexColor(r, g, b);
	end
	NuNSettings.ratings = {};
	NuNSettings.ratingsT = {};
	NuNSettings.ratingsBL = {};
	for i, value in pairs(NUN_ORATINGS) do
		NuNSettings.ratings[i] = value;
	end
	for i, value in pairs(NUN_ORATINGS_TEXT) do
		NuNSettings.ratingsT[i] = value;
	end
	for i=1, maxRatings, 1 do
		NuNSettings.ratingsBL[i] = 0;
	end
	NuNOptionsFrame:Hide();
	NuNFrame:SetUserPlaced(0);
	NuNFrame:ClearAllPoints();
	NuNFrame:SetPoint("CENTER", UIParent, "CENTER", 220, 15);
	NuNGNoteFrame:SetUserPlaced(0);
	NuNGNoteFrame:ClearAllPoints();
	NuNGNoteFrame:SetPoint("CENTER", UIParent, "CENTER", 250, -15);
	NuNSearchFrame:SetUserPlaced(0);
	NuNSearchFrame:ClearAllPoints();
	NuNSearchFrame:SetPoint("TOPLEFT", UIParent, "TOPLEFT", 60, -144);
	if ( NuNMicroFrame:IsVisible() ) then
		NuNMicroFrame:Hide();
	end
	NuNMicroFrame:SetUserPlaced(0);
	NuNMicroFrame:ClearAllPoints();
	NuNMicroFrame:SetPoint("TOP", UIParent, "TOP", 0, -30);
	NuNSettings[local_player.realmName].nunFont = nil;										-- 5.60
	NuN_UpdateFont("Fonts\\FRIZQT__.TTF", 12);								-- 5.60
	NuNMicroFrame:Show();
	NuN_Options();
end


-- Nothing to do with the new NuN Import/Export functionality
-- Update WoW Friends/Ignores based on Saved Data
function NuNOptions_Import()
	local x;
	local idx;
	local value;
	local isInGuild = false;
	local lGuild = GetGuildInfo("player");
	if ( ( lGuild ) and ( lGuild ~= "" ) ) then
		isInGuild = true;
	end

	locals.NuN_FriendIgnoreActivity = true;
	locals.NuN_IgnoreUpdate.func = nil;
	locals.NuN_IgnoreUpdate.name = nil;
	locals.NuN_IgnoreUpdate.time = 0;
	locals.NuN_FriendUpdate.func = nil;
	locals.NuN_FriendUpdate.name = nil;
	locals.NuN_FriendUpdate.time = 0;

	for idx, value in pairs(locals.NuNDataPlayers) do
		if ( ( idx ~= UNKNOWN ) and ( idx ~= UNKNOWNOBJECT ) ) then -- 5.60
			if ( ( locals.NuNDataPlayers[idx].faction ) and ( idx == locals.player_Name ) ) then
				locals.NuNDataPlayers[idx].type = NuNC.NUN_SELF_C;

			elseif ( locals.NuNDataPlayers[idx].faction == local_player.factionName ) then
				if ( locals.NuNDataPlayers[idx].ignoreLst ) then
					if ( ( not NuN_Is_Ignored(idx) ) and ( not NuNSettings[local_player.realmName].gNotIgnores[idx] ) ) then
						NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
						if ( not NuNSettings[local_player.realmName].autoS ) then
							NuN_Message(IGNORE.." "..idx);
						end
						AddIgnore(idx);
					end
				end

				if  ( ( isInGuild ) and ( locals.NuNDataPlayers[idx].guild == lGuild ) and ( not NuNSettings[local_player.realmName].autoG ) ) then
					-- Forget this entry as they are guild mates with current player and settings say not to add as friend

				elseif ( locals.NuNDataPlayers[idx].friendLst ) then
					if ( ( not NuN_Is_Friendly(idx) ) and ( not NuNSettings[local_player.realmName].gNotFriends[idx] ) ) then
						NuN_AttemptedFriendIgnores = NuN_AttemptedFriendIgnores + 1;
						if ( not NuNSettings[local_player.realmName].autoS ) then
							NuN_Message(FRIENDS.." "..idx);
						end
						AddFriend(idx);
					end
				end
			end
		end
	end

	locals.NuN_FriendIgnoreActivity = nil;
end


-- Nothing to do with the new NuN Import/Export functionality
-- Update Saved Data based on WoW Friends/Ignores
function NuNOptions_Export()
	local iName;
	for i = 1, GetNumFriends(), 1 do
		iName = GetFriendInfo(i);
		if ( ( iName ~= UNKNOWN ) and ( iName ~= UNKNOWNOBJECT ) ) then -- 5.60
			if ( locals.NuNDataPlayers[iName] ) then
				locals.NuNDataPlayers[iName].friendLst = true;
			else
				locals.NuNDataPlayers[iName] = {};
				locals.NuNDataPlayers[iName].type = NuNC.NUN_AUTO_C;
				locals.NuNDataPlayers[iName].faction = local_player.factionName;
				locals.NuNDataPlayers[iName][locals.txtTxt] = NUN_AUTO_FRIEND..NuNF.NuN_GetDateStamp();
				locals.NuNDataPlayers[iName].friendLst = true;
			end
			if ( NuNSettings[local_player.realmName].gNotFriends[iName] ) then
				NuNSettings[local_player.realmName].gNotFriends[iName] = nil;
			end
		end
	end
	for i = 1, GetNumIgnores(), 1 do
		iName = GetIgnoreName(i);
		if ( ( iName ~= UNKNOWN ) and ( iName ~= UNKNOWNOBJECT ) ) then -- 5.60
			if ( locals.NuNDataPlayers[iName] ) then
				locals.NuNDataPlayers[iName].ignoreLst = true;
			else
				locals.NuNDataPlayers[iName] = {};
				locals.NuNDataPlayers[iName].type = NuNC.NUN_AUTO_C;
				locals.NuNDataPlayers[iName].faction = local_player.factionName;
				locals.NuNDataPlayers[iName][locals.txtTxt] = NUN_AUTO_IGNORE..NuNF.NuN_GetDateStamp();
				locals.NuNDataPlayers[iName].ignoreLst = true;
			end
			if ( NuNSettings[local_player.realmName].gNotIgnores[iName] ) then
				NuNSettings[local_player.realmName].gNotIgnores[iName] = nil;
			end
		end
	end
end


-- This is executed when you click on the "Search" button on the Options Frame (i.e. this is the function that actually opens the Browser Frame)
-- Perhaps a better name would have been "Filter" - it filters notes based on the Drop Down Box available on the Options Frame
-- e.g. Just show Quest Notes, or show Player Notes with a Profession drop down box for further filtering
function NuNOptions_Search()
	local lDisplay = NUN_SEARCHFOR[locals.dropdownFrames.ddSearch].Display;	-- locals.dropdownFrames.ddSearch must be set before calling this function
	local lCommand = NUN_SEARCHFOR[locals.dropdownFrames.ddSearch].Command;	-- i.e. locals.dropdownFrames.ddSearch controls how records are filtered when the Browser frame is displayed

	ddClassSearch = nil;
	ddProfSearch = nil;
	ddQHSearch = nil;		-- 5.60
	locals.lastBttnIndex = 0;
	locals.lastBttn = nil;
	locals.lastBttnDetl = nil;
	locals.NuN_Filtered = nil;
	filterText = "";
	locals.foundNuN = {};
	NuNSearchFrame:SetScale(NuNSettings[local_player.realmName].pScale);
	NuNSearchFrame:Show();
	NuNOptionsFrame:Hide();
	NuNSearchFrame.searchType = lDisplay;
	NuNSearchTitleText:SetText(lDisplay);
	NuNSearchFrameSearchButton:Enable();
	NuNSearchFrameSearchButton:Show();

	NuNSearchFrame_MassDelete:Show();
	NuNSearchFrame_Export:Show();
	NuNExtraOptions:Show();
	NuNSearchFrame.qh = nil;

	if ( ( lCommand == "All" ) or ( strfind(lCommand, "Notes") ) ) then
		NuNSearchClassDropDown:Hide();
		NuNSearchProfDropDown:Hide();
		NuNSearchTextBox:Hide();
		NuNSearchSubSet:Hide();
		NuNSearchQHDropDown:Hide();								-- 5.60
		NuNSearch_Search();

	elseif ( lCommand == "Class" ) then
		UIDropDownMenu_ClearAll(NuNSearchClassDropDown);
		NuNSearchClassDropDown:Show();
		NuNSearchProfDropDown:Hide();
		NuNSearchTextBox:Hide();
		NuNSearchSubSet:Hide();
		NuNSearchFrameSearchButton:Disable();
		NuNSearchQHDropDown:Hide();								-- 5.60
		NuNSearch_Update();

	elseif ( lCommand == "Profession" ) then
		NuNSearchClassDropDown:Hide();
		UIDropDownMenu_ClearAll(NuNSearchProfDropDown);
		NuNSearchProfDropDown:Show();
		NuNSearchTextBox:Hide();
		NuNSearchSubSet:Hide();
		NuNSearchFrameSearchButton:Disable();
		NuNSearchQHDropDown:Hide();								-- 5.60
		NuNSearch_Update();

	elseif ( lCommand == "Quest History" ) then
		NuN_FetchQuestHistory();

	else
		NuNSearchClassDropDown:Hide();
		NuNSearchProfDropDown:Hide();
		NuNSearchTextBox:Show();
		NuNSearchSubSet:Show();
		NuNSearchTextBox:SetFocus();
		filterText = NuNSearchTextBox:GetText();
		NuNSearchQHDropDown:Hide();								-- 5.60
		NuNSearch_Search();
	end
end


-- THIS is the routing that actually filters and searches notes for Text / Class / etc. etc.
function NuNSearch_Search(mButton)
	local idx;
	local value;
	local tstTxt = NuNSearchTextBox:GetText();
	local srchText;
	local countH = 0;
	local countA = 0;
	local countN = 0;
	local classType;
	locals.searchType = NUN_SEARCHFOR[locals.dropdownFrames.ddSearch].Command;
	local subType = "";
	local noteType = 0;
	local results = 0;
	local sortType = "Default";

	if ( locals.searchType == "Date" ) then		-- Possible future development, not implemented atm
		sortType = "Date";
		locals.searchType = "All";
	end

	if ( not mButton ) then
		mButton = "LeftButton";
	end

	if ( mButton == "LeftButton" ) then
		tstTxt = strlower(tstTxt);
	end

	if ( locals.searchType == "Class" ) then
		classType = NUN_ALLCLASSES[ddClassSearch];
	end
	if ( strfind(locals.searchType, "Notes:") ) then
		subType = strsub(locals.searchType, 7);
		locals.searchType = strsub(locals.searchType, 1, 5);
	end

	locals.foundNuN = {};
	locals.foundHNuN = {};
	locals.foundANuN = {};
	locals.foundNNuN = {};
	local skip;

	for idx, value in pairs(locals.NuNDataPlayers) do
		skip = false;

		if ( NuNSettings[local_player.realmName].hignores ) then
			local isIgnored = NuNF.NuN_IsPlayerIgnored(idx);
			if ( isIgnored ) then
--				nun_msgf("Currently ignoring %s - not including player in search results because tooltips and notes for ignored players has been disabled in the options.", idx);
				skip = true;
			end
		end

		if ( not skip ) then
			if ( locals.searchType == "All" ) then
				if ( locals.NuNDataPlayers[idx].faction == "Horde" ) then
					countH = countH + 1;
					locals.foundHNuN[countH] = idx;
				elseif ( locals.NuNDataPlayers[idx].faction == "Alliance" ) then
					countA = countA + 1;
					locals.foundANuN[countA] = idx;
				end

			elseif ( locals.searchType == "Class" ) then
				if ( locals.NuNDataPlayers[idx].faction == "Horde" ) then
					if ( NUN_HCLASSES[locals.NuNDataPlayers[idx].cls] == classType ) then
						countH = countH + 1;
						locals.foundHNuN[countH] = idx;
					end
				elseif ( locals.NuNDataPlayers[idx].faction == "Alliance" ) then
					if ( NUN_ACLASSES[locals.NuNDataPlayers[idx].cls] == classType ) then
						countA = countA + 1;
						locals.foundANuN[countA] = idx;
					end
				end

			elseif ( locals.searchType == "Profession" ) then
				if ( locals.NuNDataPlayers[idx].faction == "Horde" ) then
					if ( ( locals.NuNDataPlayers[idx].prof1 == ddProfSearch ) or ( locals.NuNDataPlayers[idx].prof2 == ddProfSearch ) ) then
						countH = countH + 1;
						locals.foundHNuN[countH] = idx;
					end
				elseif ( locals.NuNDataPlayers[idx].faction == "Alliance" ) then
					if ( ( locals.NuNDataPlayers[idx].prof1 == ddProfSearch ) or ( locals.NuNDataPlayers[idx].prof2 == ddProfSearch ) ) then
						countA = countA + 1;
						locals.foundANuN[countA] = idx;
					end
				end

			-- Need to account for non-standard letters such as Umlauts...
			-- Does the lowerCase transformation interfere (?)... she's a nosey bitch isn't she.... :P
			elseif ( locals.searchType == "Text" ) then
				if ( locals.NuNDataPlayers[idx].txt ) then
					srchText = NuNF.NuN_GetCText(idx);
					local noteTitle = idx;
					if ( not srchText ) then
						srchText = "";
					end
					if ( mButton == "LeftButton" ) then
						srchText = strlower(srchText);
						noteTitle = strlower(noteTitle);
					end
					-- still testing [idx].txt for the special case of locals.pDetl/pHeader User Definable buttons  LOWER CASE IT !!!
					if ( ( strfind(srchText, tstTxt) ) or ( strfind(locals.NuNDataPlayers[idx].txt, tstTxt) ) or ( strfind(noteTitle, tstTxt) ) ) then
						local tName = idx;
						if ( not locals.NuNDataPlayers[idx].faction ) then
							tName = nil;
							local pos = strfind(idx, locals.pDetl);
							if ( not pos ) then
								pos = strfind(idx, locals.pHead);
							end
							if ( pos ) then
								tName = strsub(idx, 1, (pos - 1));
							end
						end
						if ( ( tName ) and ( locals.NuNDataPlayers[tName] ) and ( locals.NuNDataPlayers[tName].faction ) ) then
							if ( locals.NuNDataPlayers[tName].faction == "Horde" ) then
								if ( NuNF.NuNGet_TableID(locals.foundHNuN, tName) == nil ) then
									countH = countH + 1;
									locals.foundHNuN[countH] = tName;
								end
							else
								if ( NuNF.NuNGet_TableID(locals.foundANuN, tName) == nil ) then
									countA = countA + 1;
									locals.foundANuN[countA] = tName;
								end
							end
						end
					end
				end
			end
		end
	end

	if ( ( locals.searchType ~= "Class" ) and ( locals.searchType ~= "Profession" ) ) then
		for idx, value in pairs(NuNDataRNotes) do
			if ( ( NuNDataRNotes[idx] ) and ( NuNDataRNotes[idx].type ) ) then
				noteType = NuNDataRNotes[idx].type;
			else
				noteType = 1;
			end
			if ( ( locals.searchType == "All" ) or ( locals.searchType == "Notes" ) ) then
				if ( ( subType == "" ) or ( ( subType == "Generic" ) and ( noteType == 1 ) ) or ( ( subType == "Items" ) and ( noteType == 2 ) ) or ( ( subType == "Logs" ) and ( noteType == 3 ) ) or ( ( subType == "NPCs" ) and ( noteType == 4 ) ) or ( ( subType == "Quests" ) and ( noteType == 5 ) ) or ( ( subType == "LUA" ) and ( noteType == 6 ) ) ) then
					countN = countN + 1;
					locals.foundNNuN[countN] = idx;
				end

			elseif ( locals.searchType == "Text" ) then
				srchText = NuNF.NuN_GetGText(idx);
				local noteTitle = idx;
				if ( srchText == nil ) then
					srchText = "";
				end
				if ( mButton == "LeftButton" ) then
					srchText = strlower(srchText);
					noteTitle = strlower(noteTitle);
				end
				if ( ( strfind(srchText, tstTxt)) or ( strfind(noteTitle, tstTxt ) ) ) then
					countN = countN + 1;
					locals.foundNNuN[countN] = idx;
				end
			end
		end

		for idx, value in pairs(NuNDataANotes) do
			if ( ( NuNDataANotes[idx] ) and ( NuNDataANotes[idx].type ) ) then
				noteType = NuNDataANotes[idx].type;
			else
				noteType = 1;
			end

			if ( ( locals.searchType == "All" ) or ( locals.searchType == "Notes" ) ) then
				if ( ( subType == "" ) or ( ( subType == "Generic" ) and ( noteType == 1 ) ) or ( ( subType == "Items" ) and ( noteType == 2 ) ) or ( ( subType == "Logs" ) and ( noteType == 3 ) ) or ( ( subType == "NPCs" ) and ( noteType == 4 ) ) or ( ( subType == "Quests" ) and ( noteType == 5 ) ) or ( ( subType == "LUA" ) and ( noteType == 6 ) ) ) then
					countN = countN + 1;
					locals.foundNNuN[countN] = idx;
				end

			elseif ( locals.searchType == "Text" ) then
				srchText = NuNF.NuN_GetGText(idx);
				local noteTitle = idx;
				if ( srchText == nil ) then
					srchText = "";
				end
				if ( mButton == "LeftButton" ) then
					srchText = strlower(srchText);
					noteTitle = strlower(noteTitle);
				end
				if ( ( strfind(srchText, tstTxt)) or ( strfind(noteTitle, tstTxt ) ) ) then
					countN = countN + 1;
					locals.foundNNuN[countN] = idx;
				end
			end
		end
	end

	if ( sortType == "Default" ) then
		tsort(locals.foundANuN);
		tsort(locals.foundHNuN);
		tsort(locals.foundNNuN);
		NuNF.NuN_DefaultSort();
	elseif ( sortType == "Date" ) then
		NuNF.NuN_DefaultSort();
	end

	if ( ( locals.searchType ~= "Class" ) and ( locals.searchType ~= "Profession" ) ) then
		NuNSearchTextBox:Show();
		NuNSearchSubSet:Show();
		if ( locals.NuN_Filtered ) then
			local tmpNuN = {};
			local c = 0;

			for i=1, getn(locals.foundNuN), 1 do					-- #locals.foundNuN
				if ( NuNF.NuNGet_TableID(locals.NuN_Filtered, locals.foundNuN[i]) ) then
					c = c + 1;
					tmpNuN[c] = locals.foundNuN[i];
				end
			end
			locals.foundNuN = tmpNuN;
			if ( filterText == "" ) then
				NuNSearchSubSet:SetText( "" );
			else
				NuNSearchSubSet:SetText( "{"..filterText.."}" );
			end
		else
			locals.NuN_Filtered = locals.foundNuN;
		end
	end

	results = getn(locals.foundNuN);
	NuNSearchTitleText:SetText(NuNSearchFrame.searchType.." ("..results..")");

	NuNSearch_Update();
	if ( ( locals.deletedE ) and ( locals.visibles > 0 ) and ( locals.lastBttn ~= nil ) ) then
		locals.deletedE = false;
		if ( locals.lastBttnIndex > locals.visibles ) then
			NuNSearch_HighlightRefresh(locals.lastVisible);
			NuNSearchNote_OnClick(locals.lastVisible);
		else
			NuNSearch_HighlightRefresh(locals.lastBttn);
			NuNSearchNote_OnClick(locals.lastBttn);
		end
	else
		NuNSearch_HighlightRefresh(nil);
	end
end


-- Button on the Browswer frame, goes back to the Options Frame : only available if you reached the Browser frame from the Options frame in the first place
function NuNSearch_Back()
	if ( NuNFrame:IsVisible() ) then
		NuNEditDetailsFrame:Hide();
		HideNUNFrame();
	end
	if ( NuNGNoteFrame:IsVisible() ) then
		NuNGNoteFrame:Hide();
	end
	NuNSearchFrame:Hide();
	NuNOptionsFrame:SetScale(NuNSettings[local_player.realmName].pScale);
	NuN_Options();
end


-- Refresh the Browser Window, based on the records that have been filtered in to the NuNFound array of records to display
function NuNSearch_Update()
	local theNoteIndex;
	local theOffsetNoteIndex;
	local theNote;
	local theNoteHFlag;
	local theNoteAFlag;
	local theNoteNFlag;
	local theNoteLFlag;
	local theNoteType;
	local theNoteReps;
	local numNuNFound = getn(locals.foundNuN);

--	NuNF.NuN_CheckQuestList();

	locals.visibles = 0;
	FauxScrollFrame_Update(NuNSearchListScrollFrame, numNuNFound, NuNC.NUN_SHOWN, NuNC.NUN_NOTE_HEIGHT);
	for theNoteIndex=1, NuNC.NUN_SHOWN, 1 do
		theOffsetNoteIndex = theNoteIndex + FauxScrollFrame_GetOffset(NuNSearchListScrollFrame);
		theNote = _G[ "NuNSearchNote"..theNoteIndex ];

		if ( theOffsetNoteIndex > numNuNFound ) then
			theNote:Hide();
		else
			locals.noteNameLabel = _G[ "NuNSearchNote"..theNoteIndex.."Text" ];
			theNoteHFlag = _G[ "NuNSearchNote"..theNoteIndex.."FrameHFlag" ];
			theNoteAFlag = _G[ "NuNSearchNote"..theNoteIndex.."FrameAFlag" ];
			theNoteNFlag = _G[ "NuNSearchNote"..theNoteIndex.."FrameNFlag" ];
			theNoteLFlag = _G[ "NuNSearchNote"..theNoteIndex.."FrameLFlag" ];
			theNoteType = _G[ "NuNSearchNote"..theNoteIndex.."FrameType" ];
			theNoteReps = _G[ "NuNSearchNote"..theNoteIndex.."FrameReps" ];
			theNoteReps:SetText("   ");
			theNoteLFlag:Hide();
			local typ = strsub(locals.foundNuN[theOffsetNoteIndex], 1, 1);
			local noteName = strsub(locals.foundNuN[theOffsetNoteIndex], 2);
			theNote:SetText( noteName );
			locals.noteNameLabel:SetTextColor(1, 0.82, 0, 1);
			if ( typ == NuNC.NUN_HORD_C ) then
				theNoteAFlag:Hide();
				theNoteNFlag:Hide();
				theNoteHFlag:Show();
			elseif ( typ == NuNC.NUN_ALLI_C ) then
				theNoteHFlag:Hide();
				theNoteNFlag:Hide();
				theNoteAFlag:Show();
			elseif ( typ == NuNC.NUN_QUEST_C ) then
				theNoteHFlag:Hide();
				theNoteAFlag:Hide();
				if ( ( NuNDataANotes[noteName] ) or ( NuNDataRNotes[noteName] ) ) then
					theNoteNFlag:Show();
				else
					theNoteNFlag:Hide();
				end
			else
				theNoteHFlag:Hide();
				theNoteAFlag:Hide();
				theNoteNFlag:Show();
			end
			theNote.type = typ;
			if ( typ == NuNC.NUN_HORD_C ) or ( typ == NuNC.NUN_ALLI_C ) then
				if ( locals.NuNDataPlayers[noteName].type ) then
					typ = locals.NuNDataPlayers[noteName].type;
					if ( typ == NuNC.NUN_AUTO_C ) then
						theNoteType:SetText(NUN_AUTO);
					elseif ( noteName == locals.player_Name ) then
						theNoteType:SetText(NUN_PLAYER);
					elseif ( typ == NuNC.NUN_SELF_C ) then
						theNoteType:SetText(NUN_SELF);
					elseif ( typ == NuNC.NUN_MANU_C ) then
						theNoteType:SetText(NUN_MANU);
					elseif ( typ == NuNC.NUN_PARTY_C ) then
						theNoteType:SetText(NuN_Strings.NUN_PARTY);
					elseif ( typ == NuNC.NUN_GUILD_C ) then
						theNoteType:SetText(NuN_Strings.NUN_GUILD);
					else
						theNoteType:SetText("   ");
					end
					if ( ( NuNSettings[local_player.realmName].autoP ) and ( locals.NuNDataPlayers[noteName][locals.player_Name] ) and ( locals.NuNDataPlayers[noteName][locals.player_Name].partied ) ) then
						theNoteReps:SetText("x"..locals.NuNDataPlayers[noteName][locals.player_Name].partied);
					end
				else
					theNoteType:SetText("   ");
				end
			elseif ( typ == NuNC.NUN_QUEST_C ) then
				theNoteLFlag:Hide();
				if ( ( not locals.NuNQuestLog[noteName] ) and ( ( NuNQuestHistory[noteName].handedIn ) or ( NuNQuestHistory[noteName].complete ) ) ) then
					theNoteLFlag:Show();
				elseif ( ( NuNQuestHistory[noteName] ) and ( NuNQuestHistory[noteName].abandoned ) ) then
					locals.noteNameLabel:SetTextColor(0.9, 0, 0, 0.9);
				elseif ( locals.NuNQuestLog[noteName] ) then
					locals.noteNameLabel:SetTextColor(0, 0.9, 0, 0.9);
				end
				theNoteType:SetText(NuNQuestHistory[noteName].pLevel);
			else
				if ( ( NuNDataRNotes[noteName] ) and ( NuNDataRNotes[noteName].type ) ) then
					typ = NuNDataRNotes[noteName].type;
				elseif ( NuNDataANotes[noteName] ) then
					theNoteLFlag:Show();
					if ( NuNDataANotes[noteName].type ) then
						typ = NuNDataANotes[noteName].type;
					end
				else
					typ = 1;
				end
				theNoteType:SetText(NUN_NOTETYPES[typ].Display);
				if ( NUN_NOTETYPES[typ].Command == "QST" ) then
					if ( locals.NuNQuestLog[noteName] ) then
						locals.noteNameLabel:SetTextColor(0, 0.9, 0, 0.9);
					elseif ( ( NuNQuestHistory[noteName] ) and ( NuNQuestHistory[noteName].abandoned ) ) then
						locals.noteNameLabel:SetTextColor(0.9, 0, 0, 0.9);
					end
				end
			end
			theNote:Show();
			locals.visibles = locals.visibles + 1;
			locals.lastVisible = theNote;
			if ( theOffsetNoteIndex == locals.lastBttnDetl ) then
				theNote:LockHighlight();
			else
				theNote:UnlockHighlight();
			end
		end
	end
end


-- helper tooltip
function NuNSearchNote_OnEnter(bttnNote)
	local bttnName = bttnNote:GetName();
	local storePinned = NuN_PinnedTooltip.type;

	local x, y = GetCursorPosition();
	if ( x > 500 ) then
		NuN_Tooltip:SetOwner(bttnNote, "ANCHOR_LEFT");
	else
		NuN_Tooltip:SetOwner(bttnNote, "ANCHOR_RIGHT");
	end
	locals.ttName = bttnNote:GetText();
	NuN_Tooltip:ClearLines();
	if ( bttnNote.type == "N" ) then
		NuN_PinnedTooltip.type = "General";
	elseif ( bttnNote.type == "A" ) or ( bttnNote.type == "H" ) then
		NuN_PinnedTooltip.type = "Contact";
	elseif ( bttnNote.type == NuNC.NUN_QUEST_C ) then
		NuN_PinnedTooltip.type = "QuestHistory";
	end
	NuNF.NuN_BuildTT(NuN_Tooltip);
	NuN_PinnedTooltip.type = storePinned;
	NuN_State.NuN_Fade = false;
	NuN_Tooltip:Show();
end


-- This code executes when you click on a Note in the Note Browswer frame, to past its title in to Chat or another note, etc. OR just to open the Note in its NuN editing frame
function NuNSearchNote_OnClick(bttnNote, mButton)
	local noteName = bttnNote:GetText();
	local lclNote;
	local lclNoteHFlag;
	local lclNoteAFlag;
	local lclNoteNFlag;
	local lastBttnDetlN;
	local nType;

	if ( not mButton ) then
		mButton = "LeftButton";
	end

	if ( IsShiftKeyDown() ) then
		if ( ChatFrame1EditBox:IsVisible() ) then
			ChatFrame1EditBox:Insert(noteName);
		elseif ( NuNGNoteFrame:IsVisible() ) then
			local cText = NuNGNoteTextScroll:GetText().."\n"..noteName;
			NuNGNoteTextScroll:SetText(cText);
		elseif ( NuNFrame:IsVisible() ) then
			local cText = NuNText:GetText().."\n"..noteName;
			NuNText:SetText(cText);
		end
	else
		locals.lastBttn = bttnNote;
		locals.lastBttnIndex = bttnNote:GetID();
		lastBttnDetlN = bttnNote.type..noteName;
		locals.lastBttnDetl = NuNF.NuNGet_TableID(locals.foundNuN, lastBttnDetlN);

		NuNSearch_HighlightRefresh(bttnNote);

		if ( ( bttnNote.type == NuNC.NUN_HORD_C ) or ( bttnNote.type == NuNC.NUN_ALLI_C ) ) then
			if ( mButton == "LeftButton" ) then
				NuN_ShowSavedNote(noteName);
			else
				NuN_PinnedTooltipToggle(bttnNote, "Contact", noteName, true);
			end
		else
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
				return;
			end
			if ( bttnNote.type == NuNC.NUN_QUEST_C ) then
				NuNGNoteFrame.fromQuest = "1";
				contact.type = NuNGet_CommandID(NUN_NOTETYPES, "QST");
			else
				NuNGNoteFrame.fromQuest = nil;
			end
			if ( ( NuNGNoteFrame.fromQuest ) and ( mButton == "RightButton" ) ) then
				NuNGNoteFrame.fromQuest = noteName;
				StaticPopup_Show("NUN_DELETE_QUESTHISTORY");
				return;
			end
			if ( ( NuNDataANotes[noteName] ) or ( NuNDataRNotes[noteName] ) ) then
				if ( mButton == "LeftButton" ) then
					local_player.currentNote.general = noteName;
					NuN_ShowSavedGNote();
				else
					NuN_PinnedTooltipToggle(bttnNote, "General", noteName, true);
				end
			end
		end
	end
end


-- highlighting of the selected note in the Browser
function NuNSearch_HighlightRefresh(tstNote)
	local theNote;
	for i=1, NuNC.NUN_SHOWN, 1 do
		theNote = _G["NuNSearchNote"..i];
		if ( ( theNote == tstNote ) and ( theNote:IsVisible() ) ) then
			theNote:LockHighlight();
		else
			theNote:UnlockHighlight();
		end
	end
end



function NuN_DateStamp()
	NuNText:SetText(NuNText:GetText().."\n"..NuNF.NuN_GetDateStamp());
end

function NuNGNote_DateStamp()
	NuNGNoteTextScroll:SetText(NuNGNoteTextScroll:GetText().."\n"..NuNF.NuN_GetDateStamp());
end


function NuN_Loc()
	NuNText:SetText(NuNText:GetText().."\n"..NuNF.NuN_GetLoc());
end

function NuNGNote_Loc()
	NuNGNoteTextScroll:SetText(NuNGNoteTextScroll:GetText().."\n"..NuNF.NuN_GetLoc());
end

-- Create note for "Self"
NuN_AutoNote = function()
	if ( ( NuNSettings ) and ( local_player.realmName ) and ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].autoN ) ) then
		local lName;
		local lRace;
		local lClass;
		local lSex;
		local lPvPRank;
		local lPvPRankID;
		local lgName;
		local lgRank;
		local lgRankIndex;
		local bttnKey;

		if ( local_player.factionName ~= nil ) then
			locals.NuNDataPlayers[locals.player_Name] = {};
			locals.NuNDataPlayers[locals.player_Name].type = NuNC.NUN_SELF_C;
			locals.NuNDataPlayers[locals.player_Name].faction = local_player.factionName;
			locals.NuNDataPlayers[locals.player_Name][locals.txtTxt] = "";
			lRace = UnitRace("player");
			if ( lRace ~= nil ) then
				locals.dropdownFrames.ddRace = NuNF.NuNGet_TableID(locals.Races, lRace);
				locals.NuNDataPlayers[locals.player_Name].race = locals.dropdownFrames.ddRace;
			end
			lClass = UnitClass("player");
			if ( lClass ~= nil ) then
				locals.dropdownFrames.ddClass = NuNF.NuNGet_TableID(locals.Classes, lClass);
				locals.NuNDataPlayers[locals.player_Name].cls = locals.dropdownFrames.ddClass;
			end
			lSex = UnitSex("player");
			if ( lSex ~= nil ) then
				locals.NuNDataPlayers[locals.player_Name].sex = lSex - 1;
			end
			lgName, lgRank, lgRankIndex = GetGuildInfo("player");
			if ( lgName ~= "" ) and ( lgName ~= nil ) then
				bttnKey = locals.player_Name .. locals.pDetl .. "1";
				locals.NuNDataPlayers[bttnKey] = {};
				locals.NuNDataPlayers[bttnKey].txt = lgName;
				bttnKey = locals.player_Name .. locals.pDetl .. "2";
				locals.NuNDataPlayers[bttnKey] = {};
				if ( lgRankIndex == 0 ) then
					locals.NuNDataPlayers[bttnKey].txt = ( "GM : "..lgRank );
				else
					locals.NuNDataPlayers[bttnKey].txt = ( lgRankIndex.." : "..lgRank );
				end
			end
		end
	end
end


-- Clear the last changed Drop Down box in the Contact Note Frame - only allows 1 Drop down box Undo effectively
function NuN_ClrDD()
	if ( locals.lastDD == "Race" ) then
		UIDropDownMenu_ClearAll(locals.NuNRaceDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].race ) ) then
			locals.dropdownFrames.ddRace = -1;
		else
			locals.dropdownFrames.ddRace = nil;
		end
	elseif ( locals.lastDD == "Class" ) then
		UIDropDownMenu_ClearAll(locals.NuNClassDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].cls ) ) then
			locals.dropdownFrames.ddClass = -1;
		else
			locals.dropdownFrames.ddClass = nil;
		end
	elseif ( locals.lastDD == "Sex" ) then
		UIDropDownMenu_ClearAll(NuNSexDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].sex ) ) then
			locals.dropdownFrames.ddSex = -1;
		else
			locals.dropdownFrames.ddSex = nil;
		end
	elseif ( locals.lastDD == "Prof1" ) then
		UIDropDownMenu_ClearAll(NuNProf1DropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prof1 ) ) then
			locals.dropdownFrames.ddProf1 = -1;
		else
			locals.dropdownFrames.ddProf1 = nil;
		end
	elseif ( locals.lastDD == "Prof2" ) then
		UIDropDownMenu_ClearAll(NuNProf2DropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prof2 ) ) then
			locals.dropdownFrames.ddProf2 = -1;
		else
			locals.dropdownFrames.ddProf2 = nil;
		end
--	elseif ( locals.lastDD == "CRank" ) then
--		UIDropDownMenu_ClearAll(NuNCRankDropDown);
--		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].crank ) ) then
--			ddCRank = -1;
--		else
--			ddCRank = nil;
--		end
	elseif ( locals.lastDD == "Arena" ) then
		UIDropDownMenu_ClearAll(NuNArenaRDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].arena ) ) then
			locals.dropdownFrames.ddArena = -1;
		else
			locals.dropdownFrames.ddArena = nil;
		end
	elseif ( locals.lastDD == "HRank" ) then
		UIDropDownMenu_ClearAll(locals.NuNHRankDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].hrank ) ) then
			locals.dropdownFrames.ddHRank = -1;
		else
			locals.dropdownFrames.ddHRank = nil;
		end
	elseif ( locals.lastDD == "PRating" ) then
		UIDropDownMenu_ClearAll(NuNPRatingDropDown);
		if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].prating ) ) then
			locals.dropdownFrames.ddPRating = -1;
		else
			locals.dropdownFrames.ddPRating = 0;
		end
	end
	locals.lastDD = nil;
	NuNButtonClrDD:Disable();
end


-- Show slightly different note buttons depending on the Type of General note
function NuN_GTypeDependant_Setup()
	UIDropDownMenu_SetSelectedID(NuNGTypeDropDown, contact.type);
	UIDropDownMenu_SetText(NuNGTypeDropDown, NUN_NOTETYPES[contact.type].Display);
	if ( NUN_NOTETYPES[contact.type].Command == "NPC" ) then
		NuNNPCTargetButton:Show();
	else
		NuNNPCTargetButton:Hide();
	end
end


-- open a saved general note in its NuN editing frame; fetch data from database
function NuN_ShowSavedGNote(nN)
	-- allow passing of note name
	if ( ( nN ) and ( local_player.currentNote.general ~= nN ) ) then
		local_player.currentNote.general = nN;
		if ( NuNGNoteFrame:IsVisible() ) then
			NuNGNoteFrame:Hide();
		end
	end

	if ( ( NuNGNoteFrame:IsVisible() ) and ( locals.prevNote == local_player.currentNote.general ) ) then
		NuNGNoteFrame:Hide();
	else
		if ( ( NuNDataRNotes[local_player.currentNote.general] ) and ( NuNDataRNotes[local_player.currentNote.general].type ) ) then
			contact.type = NuNDataRNotes[local_player.currentNote.general].type;
		elseif ( ( NuNDataANotes[local_player.currentNote.general] ) and ( NuNDataANotes[local_player.currentNote.general].type ) ) then
			contact.type = NuNDataANotes[local_player.currentNote.general].type;
		else
			contact.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
		end
		NuNGNoteFrame.type = contact.type;
		NuN_GTypeDependant_Setup();

		if ( NuNDataRNotes[local_player.currentNote.general] ) then
			NuN_GLevel_CheckBox:SetChecked(0);
		elseif ( NuNDataANotes[local_player.currentNote.general] ) then
			NuN_GLevel_CheckBox:SetChecked(1);
		end
		locals.NuN_GNote_OriTitle = local_player.currentNote.general;
		locals.prevNote = local_player.currentNote.general;
		if ( NuNOptionsFrame:IsVisible() ) then
			NuNOptionsFrame:Hide();
		end
		if ( NuNcDeleteFrame:IsVisible() ) then
			NuNcDeleteFrame:Hide();
		end
		NuNGNoteFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		NuNGNoteFrame:Hide();
		NuNGNoteFrame:Show();
		NuNGNoteTextBox:Hide();
		general.text = NuNF.NuN_GetGText(local_player.currentNote.general);
		if ( general.text == "" ) then
			general.text = "\n";
		end
		NuNGNoteTextScroll:SetText(general.text);
		NuNGNoteTitleButtonText:SetText(local_player.currentNote.general);
		NuNGNoteTitleButton:Show();
		if ( not NuNSettings[local_player.realmName].bHave ) then
			NuNGNoteTextScroll:SetFocus();
		end
		if ( NuNGNoteFrame.fromQuest ) then
			NuNGNoteHeader:SetText(NuNC.NUN_QUEST_NOTE);
			NuNGNoteButtonDelete:Enable();
			NuNGNoteTitleButton:Disable();
			NuNGNoteButtonSaveNote:Disable();
		else
			NuNGNoteHeader:SetText(NuNC.NUN_SAVED_NOTE);
			NuNGNoteButtonDelete:Disable();
			NuNGNoteTitleButton:Enable();
			NuNGNoteButtonSaveNote:Enable();
		end
		NuNGNoteButtonDateStamp:Enable();
		NuNGNoteButtonLoc:Enable();
		NuNGNoteButtonDelete:Enable();
		if ( ( MapNotes_OnLoad ) or ( MetaMap_Quicknote ) ) then
			NuNMapNoteButton:Enable();
		end
		NuNGOpenChatButton:Enable();
		NuNGTTCheckBoxLabel:Show();
		NuN_GTTCheckBox:Show();
		NuN_GTTCheckBox:SetChecked(0);
		if ( NuN_PinnedTooltip.type == "General" ) then
			NuN_GTTCheckBox:SetChecked( NuN_CheckPinnedBox(local_player.currentNote.general) );
		end
	end
end

-- show a new General note, having been passed a note Title
function NuN_ShowTitledGNote(GNoteText)
	if ( ( NuNGNoteFrame:IsVisible() ) and ( locals.prevNote == local_player.currentNote.general ) ) then
		NuNGNoteFrame:Hide();
	else
		if ( NuNSettings[local_player.realmName].dLevel ) then
			NuN_GLevel_CheckBox:SetChecked(1);
		else
			NuN_GLevel_CheckBox:SetChecked(0);
		end
		locals.prevNote = local_player.currentNote.general;
		NuNGNoteFrame.type = contact.type;
		NuN_GTypeDependant_Setup();
		locals.NuN_GNote_OriTitle = nil;
		if ( NuNOptionsFrame:IsVisible() ) then
			NuNOptionsFrame:Hide();
		end
		if ( NuNcDeleteFrame:IsVisible() ) then
			NuNcDeleteFrame:Hide();
		end
		NuNGNoteFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		NuNGNoteFrame:Hide();
		NuNGNoteFrame:Show();
		NuNGNoteTextScroll:SetText(GNoteText);
		NuNGNoteTextBox:Hide();
		NuNGNoteTitleButtonText:SetText(local_player.currentNote.general);
		NuNGNoteTitleButton:Show();
		if ( not NuNSettings[local_player.realmName].bHave ) then
			NuNGNoteTextScroll:SetFocus();
		end
		NuNGNoteButtonDateStamp:Enable();
		NuNGNoteButtonLoc:Enable();
		if ( NuNGNoteFrame.fromQuest ) then
			NuNGNoteHeader:SetText(NuNC.NUN_QUEST_NOTE);
			NuNGNoteButtonDelete:Enable();
			if ( ( MapNotes_OnLoad ) or ( MetaMap_Quicknote ) ) then
				NuNMapNoteButton:Enable();
			end
			NuNGNoteTitleButton:Disable();
			NuNGNoteButtonSaveNote:Disable();
		else
			NuNGNoteHeader:SetText(NuNC.NUN_NEW_NOTE);
			NuNGNoteButtonDelete:Disable();
			NuNMapNoteButton:Disable();
			NuNGNoteTitleButton:Enable();
			NuNGNoteButtonSaveNote:Enable();
		end
		NuNGOpenChatButton:Disable();
		NuNGTTCheckBoxLabel:Hide();
		NuN_GTTCheckBox:Hide();
	end
end

-- Show new untitled General note
function NuN_ShowNewGNote()
	local tstTxt = NuNGNoteTextBox:GetText();
	if ( ( NuNGNoteFrame:IsVisible() ) and ( NuNGNoteTextBox:IsVisible() ) and ( tstTxt == "" ) ) then
		NuNGNoteFrame:Hide();
	else
		if ( IsAltKeyDown() ) then
			locals.NuN_LastOpen.type = "General";
			NuN_ReOpen();
			return;
		end

		if ( NuNSettings[local_player.realmName].dLevel ) then
			NuN_GLevel_CheckBox:SetChecked(1);
		else
			NuN_GLevel_CheckBox:SetChecked(0);
		end
		contact.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
		NuNGNoteFrame.type = contact.type;
		NuNGNoteFrame.fromQuest = nil;
		NuN_GTypeDependant_Setup();
		locals.NuN_GNote_OriTitle = nil;
		if ( NuNOptionsFrame:IsVisible() ) then
			NuNOptionsFrame:Hide();
		end
		if ( NuNcDeleteFrame:IsVisible() ) then
			NuNcDeleteFrame:Hide();
		end
		if ( NuNGNoteFrame.fromQuest ) then
			NuNGNoteHeader:SetText(NuNC.NUN_QUEST_NOTE);
		else
			NuNGNoteHeader:SetText(NuNC.NUN_NEW_NOTE);
		end
		NuNGNoteButtonSaveNote:Disable();
		NuNGNoteFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		NuNGNoteFrame:Hide();
		NuNGNoteFrame:Show();
		NuNGNoteTextScroll:SetText("");
		NuNGNoteTitleButton:Hide();
		NuNGNoteTextBox:SetText("");
		NuNGNoteTextBox:Show();
		NuNGNoteTextBox:SetFocus();
		NuNGNoteButtonDelete:Disable();
		NuNMapNoteButton:Disable();
		NuNGOpenChatButton:Disable();
		NuNGTTCheckBoxLabel:Hide();
		NuN_GTTCheckBox:Hide();
	end
end


---------------------------------------------
-- Options Control Click Processing --
---------------------------------------------

function NuN_OptionsGuildCheckBox_OnClick()
	if ( NuNOptionsGuildCheckButton:GetChecked() ) then
		NuNSettings[local_player.realmName].autoG = "1";
	else
		NuNSettings[local_player.realmName].autoG = nil;
	end
end

function NuN_OptionsAddCheckBox_OnClick()
	if ( NuNOptionsAddCheckButton:GetChecked() ) then
		NuNSettings[local_player.realmName].autoA = "1";
		NuN_AttemptedFriendIgnores = 0;
		friendsPendingUpdate = friendsPendingUpdate or NuN_Update_Friends();
		ignoresPendingUpdate = ignoresPendingUpdate or NuN_Update_Ignored();
		NuNSettings[local_player.realmName].autoFI = "1";
		NuNOptionsAACheckButton:SetChecked(1);
	else
		NuNSettings[local_player.realmName].autoA = nil;
	end
end

function NuN_OptionsAACheckBox_OnClick()
	if ( NuNOptionsAddCheckButton:GetChecked() ) then
		NuNOptionsAACheckButton:SetChecked(1);
		NuNSettings[local_player.realmName].autoFI = "1";
	else
		if ( NuNOptionsAACheckButton:GetChecked() ) then
			NuNSettings[local_player.realmName].autoFI = "1";
		else
			NuNSettings[local_player.realmName].autoFI = nil;
		end
	end
end

function NuN_OptionsVerboseCheckBox_OnClick()
	if ( NuNOptionsVerboseCheckButton:GetChecked() ) then
		NuNSettings[local_player.realmName].autoS = "1";
	else
		NuNSettings[local_player.realmName].autoS = nil;
	end
end

function NuN_OptionsDeleteCheckBox_OnClick()
	if ( NuNOptionsDeleteCheckButton:GetChecked() ) then
		NuNSettings[local_player.realmName].autoD = "1";
	else
		NuNSettings[local_player.realmName].autoD = nil;
	end
end

function NuN_DefaultLevelCheckBox_OnClick()
	if ( NuN_DefaultLevelCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].dLevel = "1";
	else
		NuNSettings[local_player.realmName].dLevel = nil;
	end
end

function NuN_HelpTTCheckBox_OnClick()
	if ( NuN_HelpTTCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].toolTips = "1";
	else
		NuNSettings[local_player.realmName].toolTips = nil;
	end
end

function NuN_AutoQuestCheckBox_OnClick()
	if ( NuN_AutoQuestCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoQ = "1";
		NuNF.NuN_UpdateQuestNotes("SwitchedOn");
	else
		NuNSettings[local_player.realmName].autoQ = nil;
	end
end

function NuN_AutoNoteCheckBox_OnClick()
	if ( NuN_AutoNoteCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoN = "1";
		if ( not locals.NuNDataPlayers[locals.player_Name] ) then
			NuN_AutoNote();
		end
	else
		NuNSettings[local_player.realmName].autoN = nil;
	end
end


--[[ Toggles the visibility of the border around the micro-frame (contains the quick-access buttons, for opening notes, creating new ones, etc.) --]]
function NuN_UpdateMicroFrameBorder(self, deltaTime)
	if ( NuNMicroFrame:IsVisible() and MouseIsOver(NuNMicroFrame) ) then
		NuNMicroBorder:Show();
	else
		NuNMicroBorder:Hide();
	end
end


-- Simple, sloppy, accurate-enough (will give undesirable results if two frames are overlapping) function for determining whether a the user is currently
-- interacting with the specified frame object.
function NuN_IsFrameInteractive( frame )
	local isFrameInteractive = false;
	
	if frame ~= nil and type(frame) == "table" then
		isFrameInteractive = frame:IsVisible() and MouseIsOver(frame);
	end
	
	return isFrameInteractive;
end



function NuN_NewContact(unitType)
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end

	local Friendly;

	if ( ( unitType == "target" ) and ( local_player.currentNote.unit ~= locals.player_Name ) ) then
		if ( UnitIsFriend("player", "target") ) then
			Friendly = true;
		else
			Friendly = false;
		end
	else
		Friendly = true;
	end
	if ( ((NuN_horde) and (Friendly))  or  ((not NuN_horde) and (not Friendly)) ) then
		c_faction = "Horde";
		NuNF.NuN_HordeSetup();
	else
		c_faction = "Alliance";
		NuNF.NuN_AllianceSetup();
	end
	contact.route = "Target";
	contact.race = nil;
	contact.class = nil;
	contact.sex = nil;
	contact.prof1 = nil;
	contact.prof2 = nil;
	contact.arena = nil;
	contact.hrank = nil;
	contact.guild = nil;
	gRank = nil;
	gRankIndex = nil;
	gNote = nil;
	gOfficerNote = nil;
	NuN_ShowNote();
	NuNF.NuN_UnitInfo(false, local_player.currentNote.unit, unitType);
end


function NuN_CreateContact(contactName, tFaction)
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end

	local_player.currentNote.unit = contactName;
	contact.route = "Create";
	if ( ( tFaction == "-ch" ) or ( tFaction == "Horde" ) ) then
		c_faction = "Horde";
		NuNF.NuN_HordeSetup();
	else
		c_faction = "Alliance";
		NuNF.NuN_AllianceSetup();
	end
	contact.race = nil;
	contact.class = nil;
	contact.sex = nil;
	contact.prof1 = nil;
	contact.prof2 = nil;
	contact.arena = nil;
	contact.hrank = nil;
	contact.guild = nil;
	gRank = nil;
	gRankIndex = nil;
	gNote = nil;
	gOfficerNote = nil;
	NuN_ShowNote();
end


function NuN_TextWarning(box, tLabel)
	local lenTxt, lenTxtL, oLabel;

	lenTxt = box:GetText();
	lenTxtL = strlen(lenTxt);
	local __, nL = strgsub(lenTxt, "\n", "\n");
	lenTxtL = lenTxtL + nL;
	oLabel = _G[tLabel];
	if ( ( lenTxtL == nil ) or ( lenTxtL == 0 ) ) then
		oLabel:SetText("0 / "..NuNC.NUN_MAX_TXT_LIM);
	else
		oLabel:SetText(lenTxtL.." / "..NuNC.NUN_MAX_TXT_LIM);
	end
	if ( lenTxtL > NuNC.NUN_MAX_TXT_LIM ) then
		StaticPopup_Show("NUN_NOTELIMIT_EXCEEDED");
	end
end


function NuN_ToggleToolTips()
	if ( NuNSettings[local_player.realmName].toolTips ) then
		NuNSettings[local_player.realmName].toolTips = nil;
	else
		NuNSettings[local_player.realmName].toolTips = "1";
	end
end


-- OK, this was a pain; Needed to account for the "EVENT" delay between :SetUnit("target") and the tooltip having useful information about said target....
-- so NPCInfo_Proceed is monitored in an OnUpdate routine
function NuN_NPCInfo(funcToCall, autoHide)
	local text = UnitName("target");

	if ( not NPCInfo_Proceed ) then
		if ( ( text ) and ( text ~= "" ) and ( funcToCall ) ) then
			GameTooltip:ClearLines();
			GameTooltip:Hide();
			GameTooltip:SetOwner(UIParent, "ANCHOR_CURSOR");
			GameTooltip:SetUnit("target");
			GameTooltip:Show();
			NPCInfo_Proceed = {};
			NPCInfo_Proceed.func = funcToCall;
			NPCInfo_Proceed.autoHide = autoHide;
		end

	else
		local NPCInfo = "";
		local NPCloc;
		local NPCtimed;
		local someInfo = false;
		local listText = nil;

		NPClvl = UnitLevel("target");
		NPCcls = UnitClass("target");
		NPCclsXtra = UnitClassification("target");
		NPCtype = UnitCreatureType("target");
		NPCsex = UnitSex("target");
		if ( NPClvl ~= nil ) then
			if ( NPClvl == -1 ) then
				NPCInfo = NUN_LEVEL.." : ??     ";
			else
				NPCInfo = NUN_LEVEL.." : "..NPClvl.."     ";
			end
		end
		if ( NPCcls ~= nil ) then
			NPCInfo = NPCInfo..NUN_CLASS.." : "..NPCcls..",  ";
		end
		if ( NPCsex ~= nil ) then
			if ( NPCsex == 1 ) then
				NPCInfo = NPCInfo..",  ";
			elseif ( NPCsex == 2 ) then
				NPCInfo = NPCInfo..NUN_MALE..",  ";
			elseif ( NPCsex == 3 ) then
				NPCInfo = NPCInfo..NUN_FEMALE..",  ";
			else
				NPCInfo = NPCInfo.."??"..",  ";
			end
		end
		if ( NPCtype ~= nil ) then
			NPCInfo = NPCInfo..NPCtype.."     ";
		end
		if ( NPCclsXtra ~= "normal" ) then
			NPCInfo = NPCInfo..( strupper(NPCclsXtra) );
		end

		GameTooltip:SetUnit("target");
		NPCprof = GameTooltipTextLeft2:GetText();
		if ( ( NPCprof == nil ) or ( strfind(NPCprof, NUN_LEVEL) ) ) then
			-- skip
		else
			NPCInfo = NPCInfo.."\n"..NUN_PROF.." : "..NPCprof.."    ";
		end

		NPCtimed = NuNF.NuN_GetDateStamp();
		NPCloc = NuNF.NuN_GetLoc();

		NPCInfo = NPCInfo.."\n\n"..NPCtimed.."\n"..NPCloc;

		if ( MerchantFrame:IsVisible() ) then
			listText = NuN_BuildShoppingList();
		end

		if ( listText ) then
			NPCInfo = NPCInfo.."\n\n"..listText;
		end

		-- Reset
		NPCInfo_Proceed = nil;

		return NPCInfo;
	end
end


function NuN_ShowFriendNote()
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end

	local numFriends = GetNumFriends();
	local selectedFriend;
	if (numFriends ~= nil) and (numFriends > 0) then
		if ( FriendsFrame.selectedFriendType == FRIENDS_BUTTON_TYPE_WOW ) then
			selectedFriend = GetSelectedFriend();
		elseif ( FriendsFrame.selectedFriendType == FRIENDS_BUTTON_TYPE_BNET ) then
			selectedFriend = BNGetSelectedFriend();
		end

		if ( selectedFriend ) then
			if ( FriendsFrame.selectedFriendType == FRIENDS_BUTTON_TYPE_WOW ) then
				local_player.currentNote.unit, locals.discard--[[level]], contact.class, locals.discard--[[area]], connected = GetFriendInfo(selectedFriend);
			elseif ( FriendsFrame.selectedFriendType == FRIENDS_BUTTON_TYPE_BNET ) then
				local presenceID, firsstname, surName, toonID, client;
				presenceID, firstname, surName, contact.class, toonID, client, connected = BNGetFriendInfo(selectedFriend);
				if ( firstname and surName ) then
					local_player.currentNote.unit = format(BATTLENET_NAME_FORMAT, firstname, surName);
				elseif ( firstname ~= nil ) then
					local_player.currentNote.unit = firstname;
				elseif ( surName ~= nil ) then
					local_player.currentNote.unit = surName;
				end
				if ( not firstname or not surName ) then
					connected = false;
				end
			end
--			NuN_Message("  -->> ShowFriendNode selectedFriend:" .. tostring(selectedFriend) .. "  name:" .. local_player.currentNote.unit);

			if ( contact.class == UNKNOWN ) then		-- 5.60
				contact.class = nil;
			end;
			contact.race = nil;
			contact.sex = nil;
			contact.prof1 = nil;
			contact.prof2 = nil;
			contact.arena = nil;
			contact.hrank = nil;
			contact.guild = nil;
			gRank = nil;
			gRankIndex = nil;
			gNote = nil;
			gOfficerNote = nil;
			contact.route = "Friend";

			if ( NuN_horde ) then
				NuNF.NuN_HordeSetup();
			else
				NuNF.NuN_AllianceSetup();
			end

			NuN_ShowNote();
		end
	end
end



function NuN_ShowIgnoreNote(clickedButton)
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end
	
	if not clickedButton then
		local btnText;
		btnText, clickedButton = NuN_GetName_FrameButton(FriendsFrame.GetSelectedIgnore(), NuNC.UPDATETAG_IGNORE);
		NuN_Message("HAD TO RETRIEVE THE BUTTONS FOR SELECTED IGNORE MANUALLY - CAME UP WITH '" .. tostring(btnText) .. "'");
	end
	
	if clickedButton then

--@{evo
--	local numIgnores = GetNumIgnores();
--	if (numIgnores ~= nil) and (numIgnores > 0) then
--		FriendsFrame.selectedIgnore = GetSelectedIgnore();
--@}
--		if ( FriendsFrame.selectedIgnore ) then
			local_player.currentNote.unit = GetIgnoreName(clickedButton.index);
			contact.class = nil;
			contact.race = nil;
			contact.sex = nil;
			contact.prof1 = nil;
			contact.prof2 = nil;
			contact.arena = nil;
			contact.hrank = nil;
			contact.guild = nil;
			gRank = nil;
			gRankIndex = nil;
			gNote = nil;
			gOfficerNote = nil;
			contact.route = "Ignore";
			if ( NuN_horde ) then
				NuNF.NuN_HordeSetup();
			else
				NuNF.NuN_AllianceSetup();
			end
			NuN_ShowNote();
--		end
	end
end



function NuN_ShowGuildNote()
	if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
		return;
	end

	local numGuildMembers = GetNumGuildMembers();
	if (numGuildMembers ~= nil) and (numGuildMembers > 0) then
		contact.class = nil;
		contact.race = nil;
		local_player.currentNote.unit, gRank, gRankIndex, locals.discard, contact.class, locals.discard, locals.discard, gNote, gOfficerNote, locals.discard = GetGuildRosterInfo( GetGuildRosterSelection() );
		if ( local_player.currentNote.unit ~= nil ) then
			contact.guild = GetGuildInfo("player");
			contact.route = "Guild";
			if ( NuN_horde ) then
				NuNF.NuN_HordeSetup();
			else
				NuNF.NuN_AllianceSetup();
			end
			NuN_ShowNote();
		end
		contact.sex = nil;
		contact.prof1 = nil;
		contact.prof2 = nil;
		contact.arena = nil;
		contact.hrank = nil;
	end
end

-- Update Note buttons on Social Frames
--@todo ronp - need to figure out how to make sure this is called when the player is scrolling through the guild list
NuN_UpdateNoteButton = function(nBttn, nBttnID, refreshType)
	local bName = nBttn:GetName();
	local pBttnTxt, qHeader;
	local bttnNoteHFlag = _G[bName.."FrameHFlag"];
	local bttnNoteAFlag = _G[bName.."FrameAFlag"];
	local bttnNoteNFlag = _G[bName.."FrameNFlag"];
	local bttnNoteQFlag = _G[bName.."FrameQFlag"];

	local GuildSummaryFrame	= _G[NuNC.GUILDFRAME_SUMMARY .. "Frame"];
	local GuildRosterFrame	= _G[NuNC.GUILDFRAME_ROSTER .. "Frame"];
	local GuildNewsFrame	= _G[NuNC.GUILDFRAME_NEWS .. "Frame"];
	local GuildRewardsFrame	= _G[NuNC.GUILDFRAME_REWARDS .. "Frame"];
	local GuildInfoFrame	= _G[NuNC.GUILDFRAME_INFO_EVENTS .. "Frame"];
	
	-- UPDATETAG_ANY means that we should try to determine which social frame is active 
	if ( refreshType == NuNC.UPDATETAG_ANY ) then
		if ( NuN_IsFrameInteractive(FriendsListFrame) ) then
			refreshType = NuNC.UPDATETAG_FRIEND;
		elseif ( NuN_IsFrameInteractive(IgnoreListFrame) ) then
			refreshType = NuNC.UPDATETAG_IGNORE;
		elseif ( NuN_IsFrameInteractive(WhoFrame) ) then
			refreshType = NuNC.UPDATETAG_WHO;
		elseif ( NuN_IsFrameInteractive(QuestFrame) ) then
			refreshType = NuNC.NUN_QUEST_C;
		end
		-- guild lists now in their own frame, so must update seperately
		if ( NuN_IsFrameInteractive(GuildSummaryFrame) ) then
			refreshType = NuNC.UPDATETAG_GUILD_SUMMARY;
		elseif ( NuN_IsFrameInteractive(GuildRosterFrame) ) then
			refreshType = NuNC.UPDATETAG_GUILD_ROSTER;
		elseif ( NuN_IsFrameInteractive(GuildNewsFrame) ) then
			refreshType = NuNC.GUILDFRAME_NEWS;
		elseif ( NuN_IsFrameInteractive(GuildRewardsFrame) ) then
			refreshType = NuNC.GUILDFRAME_REWARDS;
		elseif ( NuN_IsFrameInteractive(GuildInfoFrame) ) then
			refreshType = NuNC.GUILDFRAME_INFO_EVENTS;
		end
	end
	local isGuildRefresh = (refreshType == NuNC.UPDATETAG_GUILD_SUMMARY or refreshType == NuNC.UPDATETAG_GUILD_ROSTER or refreshType == NuNC.GUILDFRAME_NEWS
							or refreshType == NuNC.GUILDFRAME_REWARDS or refreshType == NuNC.GUILDFRAME_INFO_EVENTS);
							
--nun_msgf("refreshType:%s  isGuildRefresh:%s  FriendsFrameVisible:%s", tostring(refreshType), tostring(isGuildRefresh), tostring(FriendsFrame:IsVisible()));
	if ( bttnNoteAFlag and bttnNoteHFlag and bttnNoteNFlag and
		(isGuildRefresh or (FriendsFrame:IsVisible() and refreshType ~= NuNC.NUN_QUEST_C)) ) then
		local btn;
		pBttnTxt, btn = NuN_GetName_FrameButton(nBttnID, refreshType);
		
		-- make sure we're not displaying a notes
		if ( btn == nil ) then
			nBttn:Hide();
		else
			nBttn:Show();
		end
--[-[
if locals.NuNDebug then
	if ( btn ) then
	nun_msgf("#####   NuN_UpdateNoteButton #######   - refreshType:%s   pBttnTxt:%s    nBttnID:%s  (guildIndex:%s  =>  %s)", tostring(refreshType), tostring(pBttnTxt), tostring(nBttnID), tostring(nBttn.guildIndex), tostring(btn.guildIndex));
	else
	nun_msgf("#####   NuN_UpdateNoteButton #######   - refreshType:%s   pBttnTxt:%s    nBttnID:%s  (guildIndex:%s  =>  %s)", tostring(refreshType), tostring(pBttnTxt), tostring(nBttnID), tostring(nBttn.guildIndex), "NIL>NIL>");
	end
end
--]]

		if ( locals.NuNDataPlayers[pBttnTxt] ) then
			if ( locals.NuNDataPlayers[pBttnTxt].faction == "Horde" ) then
				bttnNoteAFlag:Hide();
				bttnNoteNFlag:Hide();
				bttnNoteHFlag:Show();
			else
				bttnNoteHFlag:Hide();
				bttnNoteNFlag:Hide();
				bttnNoteAFlag:Show();
			end
		else
			bttnNoteAFlag:Hide();
			bttnNoteHFlag:Hide();
			bttnNoteNFlag:Show();
		end
	end
	
--[[
	elseif ( ( QuestLogFrame:IsVisible() and not true ) and ( bttnNoteQFlag ) and ( bttnNoteNFlag ) ) then
		local lOffset = nBttnID + FauxScrollFrame_GetOffset(QuestLogScrollFrame);
		pBttnTxt, locals.discard, locals.discard, locals.discard, qHeader = GetQuestLogTitle(lOffset);
		if ( qHeader ) then
			nBttn:Hide();
		elseif ( pBttnTxt ) then
			nBttn:Show();
			local __, __, modifiedText = strfind(pBttnTxt, ".+]%s*(.+)");
			if ( modifiedText ) then pBttnTxt = modifiedText; end
			if ( ( NuNDataRNotes[pBttnTxt] ) or ( NuNDataANotes[pBttnTxt] ) ) then
				bttnNoteNFlag:Hide();
				bttnNoteQFlag:Show();
			else
				bttnNoteQFlag:Hide();
				bttnNoteNFlag:Show();
			end
		end
	end
--]]

end


-- causes scrolling to not work if your mouse is over the button....need to figure out which function I need to call in order to scroll
function NuN_NoteButton_OnMouseWheel(noteButton, delta)
	local pBttnTxt;
	local pBttn = nil;
	local nBttnID = noteButton:GetID();
	
	if ( NuN_IsFrameInteractive(FriendsListFrame) ) then
		pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_FRIEND);
		
	elseif ( NuN_IsFrameInteractive(IgnoreListFrame) ) then
		-- in 3.3.5 - they added a header to the Ignore panel, and the header is now the first button
		pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_IGNORE);
		
	elseif ( NuN_IsFrameInteractive(GuildFrame) ) then
		if ( NuN_IsFrameInteractive(GuildRosterFrame) ) then
			pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_GUILD_ROSTER);
--[-[
if locals.NuNDebug then
	if pBttn and pBttn.string2 then
	nun_msgf("MouseOver action - locals.ttName:%s    nBttnID:%s  pBttn:%s ==================================================================", tostring(pBttnTxt), tostring(nBttnID), pBttn.string2:GetText());
	else
	nun_msgf("MouseOver action - locals.ttName:%s    nBttnID:%s  pBttn:%s ==================================================================", tostring(pBttnTxt), tostring(nBttnID), "nil");
	end
end
--]]
		end
	elseif ( NuN_IsFrameInteractive(WhoFrame) ) then
		pBttnTxt = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_WHO);
	elseif ( NuN_IsFrameInteractive(QuestLogFrame) ) then
		lOffset = nBttnID + FauxScrollFrame_GetOffset(QuestLogScrollFrame);
		pBttnTxt, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete = GetQuestLogTitle(lOffset);
	end	
	
	if ( pBttnTxt ) then
		-- set the cached tooltip name to the name of the player we're hovering over in the guild roster
		locals.ttName = pBttnTxt;
		-- display a tooltip of the note for this player
		NuN_StaticTT();
		
		if ( pBttn == nil ) then
			noteButton:Hide();
		end
	elseif ( pBttn ~= nil ) then
		noteButton:Show();
	end
end

-- What to do when someone clicks on a Social Frames NotesUNeed button e.g. Friends Frame
function NuN_NoteButton_OnInteract(nBttnID, uAction)
	local pBttnTxt;
	local lOffset;
	local qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete;
	local qText = "";

	local pBttn = nil;
	if ( NuN_IsFrameInteractive(FriendsListFrame) ) then
		pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_FRIEND);
		if ( uAction == "Click" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
				return;
			end
			
			local friendsListButtonItem = pBttn;
--			NuN_Message("NoteButton_OnInteract(F) - friendButton.buttonType:" .. tostring(friendsListButtonItem.buttonType) .. "    friendButton.ID:" .. tostring(friendsListButtonItem.id));

			FriendsFrame_SelectFriend(friendsListButtonItem.buttonType, friendsListButtonItem.id);
			FriendsList_Update();

			if ( locals.NuNDataPlayers[pBttnTxt] ) then
				NuN_ShowSavedNote(pBttnTxt);
			else
				NuN_ShowFriendNote();
			end
		elseif ( uAction == "MouseOver" ) then
			locals.ttName = pBttnTxt;
			NuN_StaticTT();
		end
	elseif ( NuN_IsFrameInteractive(IgnoreListFrame) ) then
		-- in 3.3.5 - they added a header to the Ignore panel, and the header is now the first button
		pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_IGNORE);
		if ( uAction == "Click" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
				return;
			end

			FriendsFrame_SelectSquelched(pBttn.type, pBttn.index);
			IgnoreList_Update();

			if ( locals.NuNDataPlayers[pBttnTxt] ) then
				NuN_ShowSavedNote(pBttnTxt);
			else
				NuN_ShowIgnoreNote(pBttn);
			end
		elseif ( uAction == "MouseOver" ) then
			locals.ttName = pBttnTxt;
			NuN_StaticTT();
		end
	elseif ( NuN_IsFrameInteractive(WhoFrame) ) then
		pBttnTxt = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_WHO);
		if ( uAction == "Click" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
				return;
			end
			WhoFrame.selectedWho = _G["WhoFrameButton"..nBttnID].whoIndex;
			WhoFrame.selectedName = _G["WhoFrameButton"..nBttnID.."Name"]:GetText();
			WhoList_Update();
			if ( locals.NuNDataPlayers[pBttnTxt] ) then
				NuN_ShowSavedNote(pBttnTxt);
			else
				NuN_ShowWhoNote(pBttnTxt);
			end
		elseif ( uAction == "MouseOver" ) then
			locals.ttName = pBttnTxt;
			NuN_StaticTT();
		end

	elseif ( NuN_IsFrameInteractive(QuestLogFrame) ) then
		lOffset = nBttnID + FauxScrollFrame_GetOffset(QuestLogScrollFrame);
		pBttnTxt, qLevel, qTag, qGroup, qHeader, qCollapsed, qComplete = GetQuestLogTitle(lOffset);
		local_player.currentNote.general = pBttnTxt;
		if ( uAction == "Click" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
				return;
			end
			SelectQuestLogEntry(lOffset);
			QuestLog_Update();
			if ( qHeader ) then
				qText = "";
			else
				NuNGNoteFrame.fromQuest = nil;
				if ( ( NuNDataRNotes[local_player.currentNote.general] ) or ( NuNDataANotes[local_player.currentNote.general] ) ) then
					NuN_ShowSavedGNote();
				else
					if ( qLevel == nil ) then
						qLevel = "--";
					end
					if ( qTag == nil ) then
						qTag = "";
					end
					qText = "\n"..local_player.currentNote.general.."     "..NUN_QLVL..qLevel.."     "..qTag.."     ".."\n\n"..NuNF.NuN_BuildQuestText().."\n";
					if ( qHeader ) then
						contact.type = NuNGet_CommandID(NUN_NOTETYPES, "   ");
					else
						contact.type = NuNGet_CommandID(NUN_NOTETYPES, "QST");
					end
					NuN_ShowTitledGNote( qText );
				end
			end
		elseif ( uAction == "MouseOver" ) then
			locals.ttName = local_player.currentNote.general;
			NuN_StaticTT();
		end
	end
	
	if ( NuN_IsFrameInteractive(GuildFrame) ) then
		if ( NuN_IsFrameInteractive(GuildRosterFrame) ) then
			pBttnTxt, pBttn = NuN_GetName_FrameButton(nBttnID, NuNC.UPDATETAG_GUILD_ROSTER);
			if ( uAction == "Click" ) then
				-- ignore the click if we're still receiving a note from someone else
				if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "Contact" ) ) then
					return;
				end
				
				-- if the member detail side panel is open, close it now
				GuildMemberDetailFrame:Hide();
				
				-- then perform the same logic as GuildRosterButton_OnClick
				local memberGuildIndex = pBttn.guildIndex;
				SetGuildRosterSelection(memberGuildIndex);
				GuildFrame.selectedGuildMember = memberGuildIndex;
				
				GuildRoster_Update();
				if ( locals.NuNDataPlayers[pBttnTxt] ) then
					if ( ( IsAltKeyDown() ) and ( NuNFrame:IsVisible() ) and ( pBttnTxt ~= local_player.currentNote.unit ) ) then
						local insrt = "<ALT:"..pBttnTxt..">";
						local chkT = NuNText:GetText();
						if ( not strfind(chkT, insrt) ) then
							NuNText:Insert("\n" .. insrt);
						end
					else
						NuN_ShowSavedNote(pBttnTxt);
					end
				elseif ( not IsAltKeyDown() ) then
					NuN_ShowGuildNote();
				end
			elseif ( uAction == "MouseOver" ) then
--[[			
if pBttn and pBttn.string2 then
nun_msgf("MouseOver action - locals.ttName:%s    nBttnID:%s  pBttn:%s ==================================================================", tostring(pBttnTxt), tostring(nBttnID), tostring(pBttn.string2:GetText()));
else
nun_msgf("MouseOver action - locals.ttName:%s    nBttnID:%s  pBttn:%s ==================================================================", tostring(pBttnTxt), tostring(nBttnID), "nil");
end
--]]
				-- set the cached tooltip name to the name of the player we're hovering over in the guild roster
				locals.ttName = pBttnTxt;
				-- display a tooltip of the note for this player
				NuN_StaticTT();
			end
	--[[
	for now, let's assume (and code it as such) that you can't get here if the guild frame isn't in
	a view mode which would allow you to click on a player's name.
	--]]
		end
	end	
end


-- When clicking on a Social Frame NuN button, which frame was showing - Friends, Ignores, etc.
function NuN_GetName_FrameButton(lBttnID, refreshType)
	local lBttn, lBttnTxt;

	if ( refreshType == NuNC.UPDATETAG_FRIEND ) then
		lBttn = _G["FriendsFrameFriendsScrollFrameButton" .. lBttnID];

		if ( lBttn and lBttn.id ) then
			local friendID = lBttn.id;
			if ( lBttn.buttonType == FRIENDS_BUTTON_TYPE_WOW ) then
				lBttnTxt = GetFriendInfo(friendID);
			elseif ( lBttn.buttonType == FRIENDS_BUTTON_TYPE_BNET ) then
				lBttnTxt = "";
				local presenceID, firstName, lastName = BNGetFriendInfo(friendID);
				if ( firstName and lastName ) then
					lBttnTxt = format(BATTLENET_NAME_FORMAT, firstName, lastName);
				elseif ( firstName ~= nil ) then
					lBttnTxt = firstName;
				elseif ( lastName ~= nil ) then
					lBttnTxt = lastName;
				end
			end
		end
	elseif ( refreshType == NuNC.UPDATETAG_IGNORE ) then
		lBttn = _G["FriendsFrameIgnoreButton" .. lBttnID];
		if ( lBttn and lBttn.type ) then
			local ignoreID, unused = lBttn.index;
			if ( lBttn.type == SQUELCH_TYPE_IGNORE ) then
				lBttnTxt = GetIgnoreName(ignoreID);
			elseif ( lBttn.type == SQUELCH_TYPE_BLOCK_INVITE ) then
				unused, lBttnTxt = BNGetBlockedInfo(ignoreID);
			elseif ( lBttn.type == SQUELCH_TYPE_BLOCK_TOON ) then
				unused, lBttnTxt = BNGetBlockedToonInfo(ignoreID);
			elseif ( lBttn.type == SQUELCH_TYPE_MUTE ) then
				lBttnText = GetMuteName(ignoreID);
			end
		end
--		NuN_Message("GetName_FrameButton(I) - button:" .. tostring(lBttn) .. "    ButtonID:" .. tostring(lBttnID) .. "   buttonText:" .. tostring(lBttnTxt));
	elseif ( refreshType == NuNC.UPDATETAG_GUILD_ROSTER ) then
		local guildIndex = nil;
		
		-- for now, only have guild roster buttons, but they will change after cata comes out....
		lBttn = _G[NuNC.GUILDFRAME_ROSTER .. NuNC.GUILDFRAME_BUTTONNAME .. lBttnID];
		if lBttn then
			guildIndex = lBttn.guildIndex;
		end
		if guildIndex == nil and GuildRosterContainer ~= nil then
			local scrollOffset = HybridScrollFrame_GetOffset(GuildRosterContainer);
			guildIndex = lBttnID + scrollOffset;
		end
		--[-[
		if locals.NuNDebug then
			if lBttn and lBttn.string2 then
			nun_msgf("NuN_GetName_FrameButton >>>> mode:%s   guildIndex:%s   lBttnID:%s   lBttn:%s", locals.currentGuildRosterView, tostring(guildIndex), tostring(lBttnID), tostring(lBttn.string2:GetText()));
			else
			nun_msgf("NuN_GetName_FrameButton >>>> mode:%s   guildIndex:%s   lBttnID:%s   lBttn:%s", locals.currentGuildRosterView, tostring(guildIndex), tostring(lBttnID), "??????");
			end
		end
		--]]
		-- we'll probably need to make this a bit more general purpose once all the view modes are in...
		local nunButton = _G["NuN_GuildRosterButton"..lBttnID];
		if ( locals.currentGuildRosterView == "tradeskill" ) then
			if nunButton and lBttn then
				nunButton:SetPoint("RIGHT", lBttn, "RIGHT");
				nunButton:Show();
			end
			
			local __, playerName;
			if guildIndex ~= nil then
				__, __, __, __, __, __, playerName = GetGuildTradeSkillInfo(guildIndex);
			end
			if ( playerName ) then
				-- playerName will be nil if guildIndex corresponds to a header
				lBttnTxt = playerName;
			else
				lBttn = nil;
			end
		elseif guildIndex ~= nil then
			lBttnTxt = GetGuildRosterInfo(guildIndex);
			if ( locals.currentGuildRosterView == "achievement" ) then
				if lBttn then
					-- in this view, we need to adjust the position of the button...the most ideal place seems to be just to the left of the achievement rank text
					local playerNameString = lBttn.string2;
					if nunButton and playerNameString then
						nunButton:SetPoint("RIGHT", playerNameString, "RIGHT", 9, 0);
						nunButton:Show();
					else
						-- otherwise, clear the button result so that the caller will hide all of the NotesUNeed buttons.
						lBttn = nil;
					end
				end
			elseif nunButton and lBttn then
				nunButton:SetPoint("RIGHT", lBttn, "RIGHT");
				nunButton:Show();
			end
		end
	elseif ( refreshType == NuNC.UPDATETAG_WHO ) then
		lBttn = _G["WhoFrameButton"..lBttnID.."Name"];
		lBttnTxt = lBttn:GetText();
	elseif ( refreshType == NuNC.NUN_QUEST_C ) then
		lBttn = _G["QuestLogScrollFrameButton" ..lBttnID.. "NormalText"];
		if (lBttn) then
			lBttnTxt = lBttn:GetText();
		else
			lBttnTxt = "Error with QuestLogTitle";
		end
	end
	return lBttnTxt, lBttn;
end


function NuN_StaticTT()
	local storePinned = NuN_PinnedTooltip.type;

	NuN_Tooltip:ClearLines();
	NuN_PinnedTooltip.type = "Nil";
	NuNF.NuN_BuildTT(NuN_Tooltip);
	NuN_PinnedTooltip.type = storePinned;
	NuN_State.NuN_Fade = false;
	NuN_Tooltip:Show();
end

-- the NuN Tooltip should fade with the Game Tooltip, or WorldMap tooltip
function NuN_Tooltip_OnUpdate(self,elapsed)
	if ( ( NuN_State.NuN_Fade ) and ( not UnitExists("mouseover") ) ) then
		local gt = GameTooltipTextLeft1:GetText();
		if ( gt ~= locals.gtName ) then
			self:Hide();
			return;
		end
		if ( self.fadeStartTime == 0 ) then
			self.fadeStartTime = GetTime();
		end
		local elapsed = GetTime() - self.fadeStartTime;
		local fadeHoldTime = self.fadeHoldTime;
		local fadeOutTime = self.fadeOutTime;
		if ( elapsed >= ( fadeHoldTime + fadeOutTime ) ) then
			self:ClearLines();
			self:Hide();
		elseif  ( elapsed > fadeHoldTime ) then
			local alpha = 1 - ( ( elapsed - fadeHoldTime ) / fadeOutTime );
			self:SetAlpha(alpha);
		end
	end
end


function NuN_FFButton_OnShow()
	NuN_FFButton_Up();
end

-- There is a single note in the Header of the Social Frame which will show/create a note depending on what Frame is showing, and what player is selected
function NuN_FFButton_Down()
	if ( NuN_horde ) then
		NuN_FFButton_StateADown:Hide();
		NuN_FFButton_StateAUp:Hide();
		NuN_FFButton_StateHUp:Hide();
		NuN_FFButton_StateHDown:Show();
	else
		NuN_FFButton_StateHDown:Hide();
		NuN_FFButton_StateHUp:Hide();
		NuN_FFButton_StateAUp:Hide();
		NuN_FFButton_StateADown:Show();
	end

	if ( NuN_IsFrameInteractive(FriendsFrameRemovePlayerButton) ) then
		local Idx = BlackList:GetSelectedBlackList();
		if ( Idx > 0 ) then
			local _name = BlackList:GetNameByIndex(Idx);
			if ( ( _name ) and ( locals.NuNDataPlayers[_name] ) ) then
				NuN_ShowSavedNote(_name);
			end
		end

	elseif ( NuN_IsFrameInteractive(FriendsListFrame) ) then
		NuN_ShowFriendNote();

	elseif ( NuN_IsFrameInteractive(IgnoreListFrame) ) then
		NuN_ShowIgnoreNote();
	
	elseif( NuN_IsFrameInteractive(GuildFrame) ) then
		if ( NuN_IsFrameInteractive(GuildRosterFrame) ) then
			NuN_ShowGuildNote();
			
		-- other tabs here, after cata
		
		
		end

	elseif ( NuN_IsFrameInteractive(WhoFrame) ) then
		if ( WhoFrame.selectedName ) then
			NuN_ShowWhoNote(WhoFrame.selectedName);
		end

	elseif ( NuN_IsFrameInteractive(RaidFrame) ) then
		local tstValue = NuN_CheckTarget();
		if ( UnitInRaid("target") ) then
			local_player.currentNote.unit = UnitName("target");
			if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
				NuN_ShowSavedNote(local_player.currentNote.unit);
			else
				NuN_NewContact("target");
			end
		end
	end
end

function NuN_FFButton_Up()
	if ( NuN_horde ) then
		NuN_FFButton_StateADown:Hide();
		NuN_FFButton_StateAUp:Hide();
		NuN_FFButton_StateHDown:Hide();
		NuN_FFButton_StateHUp:Show();
	else
		NuN_FFButton_StateHDown:Hide();
		NuN_FFButton_StateHUp:Hide();
		NuN_FFButton_StateADown:Hide();
		NuN_FFButton_StateAUp:Show();
	end
end


-- The Titles of General Notes can be edited, allowing the note to be renamed
function NuN_GNoteTitle_OnClick()
	NuN_ClearPinnedTT();
	NuNGTTCheckBoxLabel:Hide();
	NuN_GTTCheckBox:Hide();
	NuNGNoteTextBox:SetText( NuNGNoteTitleButtonText:GetText() );
	NuNGNoteTitleButton:Hide();
	NuNGNoteTextBox:Show();
	NuNGNoteTextBox:SetFocus();
end

function NuN_GNoteTitleSet()
	local_player.currentNote.general = NuNGNoteTextBox:GetText();
	local_player.currentNote.general = strgsub(local_player.currentNote.general, "\124\124", "|");
	local_player.currentNote.general = strgsub(local_player.currentNote.general, "|C", "|c");
	local_player.currentNote.general = strgsub(local_player.currentNote.general, "|R", "|r");
	local_player.currentNote.general = strgsub(local_player.currentNote.general, "||c", "|c");
	local_player.currentNote.general = strgsub(local_player.currentNote.general, "||r", "|r");
	NuNGNoteTitleButtonText:SetText( local_player.currentNote.general );
	NuNGNoteTextBox:Hide();
	NuNGNoteTitleButton:Show();
end


-- NotesUNeed tooltip shows alongside the game tooltip, rather than modifying the normal tooltip itself
function NuN_GameTooltip_OnShow(self,tTip)
	local storePinned = NuN_PinnedTooltip.type;
	local p1 = 1;
	local strippedName = "";
	local sNLen = 0;
	local anchorBy, anchorTo;
	local pRating;
	local focus = GetMouseFocus();
	if ( focus ) then
		focus = focus:GetName();
		if ( ( focus ) and ( strfind( focus, "AlphaMapNotesPOI") ) ) then
			return;
		end
	end

	if ( not tTip ) then
		tTip = GameTooltip;
	end

	local tx, ty;
	tx, ty = tTip:GetCenter();
--NuN_Message("Gametooltip_OnShow - self:" .. tostring(self) .. "    tTip:" .. tostring(tTip) .. "  tx,ty:" .. tostring(tx)..","..tostring(ty));
	if ( ( not tx ) or ( not ty ) ) then
		locals.noTipAnchor = tTip;
		return;
	end

	locals.gtName = GameTooltipTextLeft1:GetText();
	if ( not locals.gtName ) then
		return;
	end

	for idx, value in ipairs(NuNSettings.ratings) do
		if ( locals.gtName == value ) then
			pRating = idx;
			break;
		end
	end

	if ( not UnitExists("mouseover") ) then
		if ( not focus ) then focus = ""; end
		if ( strfind( focus, "Container") ) then

		else
			sNLen = strlen(locals.gtName);
			for i=sNLen, 1, -1 do
				local tstChar = strsub(locals.gtName, i, i);
				if ( tstChar == " " ) then
					p1 = i + 1;
					break;
				end
			end
			strippedName = strsub(locals.gtName, p1);
			if ( locals.NuNDataPlayers[strippedName] ) then
				locals.gtName = strippedName;
			end
		end
	end
--NuN_Message("NuN_GameToolTip_OnShow - pRating:" .. tostring(pRating));
	if ( ( UnitExists("mouseover") ) or ( ( RaidFrame:IsVisible() ) and ( MouseIsOver(RaidFrame) ) ) or ( ( TargetFrame:IsVisible() ) and ( MouseIsOver(TargetFrame) ) ) ) then
		local typ = "Nil";
		if ( UnitExists("mouseover") ) then
			locals.ttName = UnitName("mouseover");
			NuN_State.NuN_Fade = true;
			if ( UnitIsPlayer("mouseover") ) then
				typ = "Contact";
				if ( not locals.NuNDataPlayers[locals.ttName] ) then
					locals.ttName = nil;
				end
			else
				typ = "General";
				if ( ( not NuNDataRNotes[locals.ttName] ) and ( not NuNDataANotes[locals.ttName] ) ) then
					locals.ttName = nil;
				end
			end
		else
			locals.ttName = locals.gtName;
			NuN_State.NuN_Fade = false;
		end
		if ( ( locals.ttName ~= nil ) and ( ( locals.NuNDataPlayers[locals.ttName] ) or ( NuNDataRNotes[locals.ttName] ) or ( NuNDataANotes[locals.ttName] ) ) ) then
			anchorBy, anchorTo = NuN_GetTipAnchor(tTip);
			NuN_Tooltip:Hide();
			NuN_Tooltip:SetOwner(tTip, "ANCHOR_NONE");
			locals.NuN_TT_Y_Offset = 0;
			NuN_PinnedTooltip.type = typ;
			NuN_State.NuN_MouseOver = true;
			NuNF.NuN_BuildTT(NuN_Tooltip);
			NuN_State.NuN_MouseOver = false;
			NuN_PinnedTooltip.type = storePinned;
			NuN_Tooltip:SetScale( tTip:GetScale() );
			NuN_Tooltip:ClearAllPoints();
			NuN_Tooltip:SetPoint(anchorBy, tTip, anchorTo, 0, 0);
			NuN_Tooltip:Show();

		else
			NuN_Tooltip:ClearLines();
			NuN_Tooltip:Hide();
		end

	elseif ( pRating ) then
		pRating = NuNSettings.ratingsT[pRating];
		if ( ( pRating ) and ( pRating ~= "" ) ) then
			pRating = NuNF.NuN_ParseTT(pRating, 80);
			GameTooltip:AddLine(pRating);
		end

	else
		locals.ttName = locals.gtName;
		if ( NuNData[locals.itmIndex_dbKey][locals.ttName] ) then
			locals.ttName = ( NuNData[locals.itmIndex_dbKey][locals.ttName] );
		end
		if ( ( locals.ttName ~= nil ) and  ( ( locals.NuNDataPlayers[locals.ttName] ) or ( NuNDataRNotes[locals.ttName] ) or ( NuNDataANotes[locals.ttName] ) ) ) then
			NuN_State.NuN_Fade = false;
			NuN_Tooltip:ClearLines();
			NuN_Tooltip:Hide();
			NuN_Tooltip:SetOwner(tTip, "ANCHOR_NONE");
			locals.NuN_TT_Y_Offset = 0;
			NuN_PinnedTooltip.type = "Nil";
			NuNF.NuN_BuildTT(NuN_Tooltip);
			NuN_PinnedTooltip.type = storePinned;
			NuN_Tooltip:SetScale( tTip:GetScale() );
			NuN_Tooltip:ClearAllPoints();
			local num1 = ShoppingTooltip1:NumLines();
			local num2 = ShoppingTooltip2:NumLines();
			if ( num2 and ( num2 > 0 ) and ShoppingTooltip2 and MerchantFrame and ( MerchantFrame:IsVisible() ) and ( MouseIsOver(MerchantFrame) ) ) or ( num2 and ( num2 > 0 ) and ShoppingTooltip2 and AuctionFrame and ( AuctionFrame:IsVisible() ) and ( MouseIsOver(AuctionFrame) ) ) then
				anchorBy, anchorTo = NuN_GetTipAnchor(ShoppingTooltip2);
				NuN_Tooltip:SetPoint(anchorBy, "ShoppingTooltip2", anchorTo, 0, 0);
			elseif ( num1 and ( num1 > 0 ) ) and ( ShoppingTooltip1 and MerchantFrame and ( MerchantFrame:IsVisible() ) and ( MouseIsOver(MerchantFrame) ) ) or ( num1 and ( num1 > 0 ) ) and ( ShoppingTooltip1 and AuctionFrame and ( AuctionFrame:IsVisible() ) and ( MouseIsOver(AuctionFrame) ) ) then
				anchorBy, anchorTo = NuN_GetTipAnchor(ShoppingTooltip1);
				NuN_Tooltip:SetPoint(anchorBy, "ShoppingTooltip1", anchorTo, 0, 0);
			else
				anchorBy, anchorTo = NuN_GetTipAnchor(tTip);
				NuN_Tooltip:SetPoint(anchorBy, tTip, anchorTo, 1, 0);
			end
			NuN_Tooltip:Show();
		else
			NuN_Tooltip:ClearLines();
			NuN_Tooltip:Hide();
		end
	end
end



function NuN_WorldMapTooltip_OnShow(id, lTooltip)
	local noPopup = true;
	local nName;

	if ( not lTooltip ) then
		lTooltip = WorldMapTooltip;
	end

	local tName = lTooltip:GetName();
	local tField = _G[tName.."TextLeft1"];
	nName = tField:GetText();

	NuNPopup:Hide();
	if ( NuNData[locals.itmIndex_dbKey][nName] ) then
		nName = ( NuNData[locals.itmIndex_dbKey][nName] );
	end

	local MNCont = nil;
	local MNZone = nil;
	local NuN_Key = nil;

	if ( MapNotes_Data_Notes ) then					-- + v5.00.11200
		MNCont = "WM ";						-- + v5.00.11200
		local cont = GetMapInfo();				-- + v5.00.11200
		if ( cont ) then					-- + v5.00.11200
			MNCont = MNCont..cont;				-- + v5.00.11200
		else							-- + v5.00.11200
			MNCont = MNCont.."WorldMap";			-- + v5.00.11200
		end							-- + v5.00.11200
		MNZone = 0;						-- + v5.00.11200

	else
		id = nil;
	end

	if ( id ) then
		NuN_Key = MNCont.."-"..MNZone.."-"..id;
	end

	if ( NuN_Key ) then
		if ( ( NuNData[locals.mrgIndex_dbKey] ) and ( NuNData[locals.mrgIndex_dbKey][NuN_Key] ) ) then
			local idx, value, lBttn, lHeight;
			local lWidth = NuNPopupTitle:GetWidth();
			local lCount = 0;
			NuNPopupButton1:SetText("");
			NuNPopupButton1:Hide();
			NuNPopupButton2:SetText("");
			NuNPopupButton2:Hide();
			NuNPopupButton3:SetText("");
			NuNPopupButton3:Hide();
			NuNPopupButton4:SetText("");
			NuNPopupButton4:Hide();
			NuNPopupButton5:SetText("");
			NuNPopupButton5:Hide();
			for idx, value in pairs(NuNData[locals.mrgIndex_dbKey][NuN_Key]) do
				if ( idx ~= "noteCounter" ) then
					if ( ( NuNDataANotes[idx] ) or ( NuNDataRNotes[idx] ) ) then
						lCount = lCount + 1;
						lBttn = _G["NuNPopupButton"..lCount];
						lBttn.note = idx;
						lBttn:SetText(idx);
						lBttn:Show();
						local tWidth = lBttn:GetTextWidth();
						if ( ( tWidth ) and ( tWidth > lWidth ) ) then
							lWidth = tWidth;
						end
					end
				end
			end
			if ( lCount > 0 ) then
				local lHeight = math.ceil( ((20*(lCount + 1)) + 10) );
				lWidth = math.ceil( (lWidth) * 1.15 );
				if ( lCount > 2 ) then
					lHeight = lHeight - (lCount * lCount);
				end
				NuNPopup:Hide();
				NuNPopup:ClearAllPoints();
				NuNPopup:SetHeight(lHeight);
				NuNPopup:SetWidth(lWidth);
				NuNPopupButton1:SetWidth(lWidth - 4);
				NuNPopupButton2:SetWidth(lWidth - 4);
				NuNPopupButton3:SetWidth(lWidth - 4);
				NuNPopupButton4:SetWidth(lWidth - 4);
				NuNPopupButton5:SetWidth(lWidth - 4);
				NuNPopup.id = id;
				local x, y = GetCursorPosition();
				if ( y > 300 ) then
					if ( x > 500 ) then
						NuNPopup:SetPoint("TOPRIGHT", lTooltip, "BOTTOMRIGHT", 0, 0);
						NuNPopup.point = "TOPRIGHT";
						NuNPopup.relativePoint = "BOTTOMRIGHT";
					else
						NuNPopup:SetPoint("TOPLEFT", lTooltip, "BOTTOMLEFT", 0, 0);
						NuNPopup.point = "TOPLEFT";
						NuNPopup.relativePoint = "BOTTOMLEFT";
					end
				else
					if ( x > 500 ) then
						NuNPopup:SetPoint("BOTTOMRIGHT", lTooltip, "TOPRIGHT", 0, 0);
						NuNPopup.point = "BOTTOMRIGHT";
						NuNPopup.relativePoint = "TOPRIGHT";
					else
						NuNPopup:SetPoint("BOTTOMLEFT", lTooltip, "TOPLEFT", 0, 0);
						NuNPopup.point = "BOTTOMLEFT";
						NuNPopup.relativePoint = "TOPLEFT";
					end
				end
				NuNPopupTitle:SetTextColor(0.1, 0.9, 0.1, 0.9);
--				locals.popUpHide = nil;
				NuNPopup:SetAlpha(1);
				NuNPopup:Show();
				NuN_MapTooltip:ClearAllPoints();
				NuN_MapTooltipShow(NuNPopupButton1.note, "NuNPopup", NuNPopup.point, NuNPopup.relativePoint, lTooltip);
				NuNPopupButton1:LockHighlight();
				noPopup = nil;
			end
		end
	end

	if ( noPopup ) then
		if ( ( locals.NuNDataPlayers[nName] ) or ( NuNDataRNotes[nName] ) or ( NuNDataANotes[nName] ) ) then
			NuN_MapTooltipShow(nName, lTooltip, nil, nil, lTooltip);
		end
	end
end


function NuN_MapTooltipShow(nName, relativeTo, point, relativePoint, tooltipOwner)
	local storePinned = NuN_PinnedTooltip.type;

	NuNPopupButton1:UnlockHighlight();
	if ( not nName ) then
		return;
	end
	locals.gtName = nName;
	locals.ttName = locals.gtName;
	if ( ( locals.ttName ~= nil ) and  ( ( locals.NuNDataPlayers[locals.ttName] ) or ( NuNDataRNotes[locals.ttName] ) or ( NuNDataANotes[locals.ttName] ) ) ) then
		NuN_State.NuN_Fade = false;
		NuN_MapTooltip:ClearLines();
		NuN_MapTooltip:Hide();
		NuN_MapTooltip:SetOwner(tooltipOwner, "ANCHOR_NONE");
		NuN_PinnedTooltip.type = "Nil";
		NuNF.NuN_BuildTT(NuN_MapTooltip);
		NuN_PinnedTooltip.type = storePinned;
		if ( ( point ) and ( relativePoint ) ) then
			NuN_MapTooltip:SetPoint(point, relativeTo, relativePoint, 0, 0);
		else
			local x, y = GetCursorPosition();
			if ( y > 300 ) then
				if ( x > 500 ) then
					NuN_MapTooltip:SetPoint("TOPRIGHT", relativeTo, "BOTTOMRIGHT", 0, 0);
				else
					NuN_MapTooltip:SetPoint("TOPLEFT", relativeTo, "BOTTOMLEFT", 0, 0);
				end
			else
				if ( x > 500 ) then
					NuN_MapTooltip:SetPoint("BOTTOMRIGHT", relativeTo, "TOPRIGHT", 0, 0);
				else
					NuN_MapTooltip:SetPoint("BOTTOMLEFT", relativeTo, "TOPLEFT", 0, 0);
				end
			end
		end
		if ( type(relativeTo) == "string" ) then
			relativeTo = _G[relativeTo];
		end
		NuN_MapTooltip:Show();
	else
		NuN_MapTooltip:ClearLines();
		NuN_MapTooltip:Hide();
	end
end


function NuN_WorldMapTooltip_OnHide()
	locals.popUpHide = true;
	if ( NuNPopup:IsVisible() ) then
		WorldMapTooltip:Show();
	else
		NuN_MapTooltip:ClearLines();
		NuN_MapTooltip:Hide();
	end
end


function NuN_ItemRefTooltip_OnShow()
	locals.gtName = ItemRefTooltipTextLeft1:GetText();
	locals.ttName = locals.gtName;

	if ( NuNData[locals.itmIndex_dbKey][locals.ttName] ) then
		locals.ttName = ( NuNData[locals.itmIndex_dbKey][locals.ttName] );
	end
	if ( ( locals.ttName ~= nil ) and ( ( NuNDataRNotes[locals.ttName] ) or ( NuNDataANotes[locals.ttName] ) ) ) then
		NuN_PinnedTooltip.noteName = locals.ttName;
		NuN_State.NuN_Fade = false;
		NuN_ClearPinnedTT();
		NuN_PinnedTooltip:SetOwner(ItemRefTooltip, "ANCHOR_TOPLEFT");
		NuN_State.NuN_PinUpHeader = true;
		NuN_PinnedTooltip.type = "General";
		NuNF.NuN_BuildTT(NuN_PinnedTooltip);
		NuN_State.NuN_PinUpHeader = false;
		NuN_PinnedTooltip:Show();
		NuN_State.pinnedTTMoved = false;
		if ( NuNGNoteFrame:IsVisible() ) then
			NuN_GTTCheckBox:SetChecked( NuN_CheckPinnedBox(locals.ttName) );
		end
	elseif ( not NuN_State.pinnedTTMoved ) then
		NuN_ClearPinnedTT();
	end
end


function NuN_ItemRefTooltip_OnHide()
	if ( not NuN_State.pinnedTTMoved ) then
		NuN_ClearPinnedTT();
	end
end


function NuN_FlagMoved()
	NuN_State.pinnedTTMoved = true;
	NuN_PinnedTooltip.x, NuN_PinnedTooltip.y = NuN_PinnedTooltip:GetCenter();
	if ( not NuNSettings[local_player.realmName].pT ) then
		-- if pT was never set, it means that this is the first time the user tried moving a tooltip and this is our first run
		NuNSettings[local_player.realmName].pT = {};
		NuNSettings[local_player.realmName].pT.type = ttType;
		NuNSettings[local_player.realmName].pT.name = locals.ttName;
	end
	NuNSettings[local_player.realmName].pT.x = NuN_PinnedTooltip.x;
	NuNSettings[local_player.realmName].pT.y = NuN_PinnedTooltip.y;
end


function NuN_GameTooltip_OnHide()
	locals.popUpHide = true;
	locals.noTipAnchor = nil;
	if ( not NuN_State.NuN_Fade ) then
		NuN_Tooltip:ClearLines();
		NuN_Tooltip:Hide();
	end
	if ( not NuNPopup:IsVisible() ) then
		NuN_MapTooltip:ClearLines();
		NuN_MapTooltip:Hide();
	end
end


function NuN_TTCheckBox_OnClick(self,frameType)
	NuN_State.pinnedTTMoved = false;
	if ( frameType == "Contact" ) then
		if ( NuN_CTTCheckBox:GetChecked() ) then
			locals.ttName = local_player.currentNote.unit;
			NuN_PinnedTooltip.noteName = local_player.currentNote.unit;
			NuN_ClearPinnedTT();
			NuN_PinnedTooltip:SetOwner(self, ANCHOR_BOTTOMRIGHT);
			NuN_State.NuN_PinUpHeader = true;
			NuN_PinnedTooltip.type = frameType;
			NuNSettings[local_player.realmName].pT = {};
			NuNSettings[local_player.realmName].pT.type = "Contact";
			NuNSettings[local_player.realmName].pT.name = locals.ttName;
			NuNF.NuN_BuildTT(NuN_PinnedTooltip);
			NuN_State.NuN_PinUpHeader = false;
			NuN_PinnedTooltip:Show();
			NuN_CTTCheckBox:SetChecked(true);
		else
			NuN_ClearPinnedTT();
		end
	elseif ( frameType == "General" ) then
		if ( NuN_GTTCheckBox:GetChecked() ) then
			locals.ttName = local_player.currentNote.general;
			NuN_PinnedTooltip.noteName = local_player.currentNote.general;
			NuN_ClearPinnedTT();
			NuN_PinnedTooltip:SetOwner(self, ANCHOR_BOTTOMRIGHT);
			NuN_State.NuN_PinUpHeader = true;
			NuN_PinnedTooltip.type = frameType;
			NuNSettings[local_player.realmName].pT = {};
			NuNSettings[local_player.realmName].pT.type = "General";
			NuNSettings[local_player.realmName].pT.name = locals.ttName;
			NuNF.NuN_BuildTT(NuN_PinnedTooltip);
			NuN_State.NuN_PinUpHeader = false;
			NuN_PinnedTooltip:Show();
			NuN_GTTCheckBox:SetChecked(true);
		else
			NuN_ClearPinnedTT();
		end
	end
end

-- Toggling of the NotesUNeed Pin up tooltip
function NuN_PinnedTooltipToggle(self, ttType, noteName, setTTOwner)
	if ( ( not ttType ) or ( not noteName ) ) then
		ttType = NuN_PinnedTooltip.type;
		noteName = NuN_PinnedTooltip.noteName;
		if ( ( not ttType ) or ( ttType == "Empty" ) or ( not noteName ) ) then
			if ( NuNGNoteFrame:IsVisible() ) then
				ttType = "General";
				noteName = local_player.currentNote.general;

			elseif ( NuNFrame:IsVisible() ) then
				ttType = "Contact";
				noteName = local_player.currentNote.unit;
	
			else
				return;
			end
		end
	end
--toggle
	if ( ( not NuN_PinnedTooltip:IsVisible() ) or ( ( NuN_PinnedTooltip:IsVisible() ) and ( noteName ~= NuN_PinnedTooltip.noteName ) ) ) then
		locals.ttName = noteName;
		NuN_PinnedTooltip.noteName = noteName;
		NuN_ClearPinnedTT();
		if ( setTTOwner ) then
			NuN_PinnedTooltip:SetOwner(self, ANCHOR_BOTTOMRIGHT);
		else
			NuN_PinnedTooltip:SetOwner(UIParent, ANCHOR_NONE);
		end
		NuN_State.NuN_PinUpHeader = true;
		NuN_PinnedTooltip.type = ttType;
		NuNSettings[local_player.realmName].pT = {};
		NuNSettings[local_player.realmName].pT.type = ttType;
		NuNSettings[local_player.realmName].pT.name = locals.ttName;
		NuNF.NuN_BuildTT(NuN_PinnedTooltip);
		NuN_State.NuN_PinUpHeader = false;
		NuN_PinnedTooltip:Show();
		if ( not setTTOwner ) then
			if ( ( NuN_PinnedTooltip.x ) and ( NuN_PinnedTooltip.y ) ) then
				NuN_PinnedTooltip:ClearAllPoints();
				NuN_PinnedTooltip:SetPoint("CENTER", "UIParent", "BOTTOMLEFT", NuN_PinnedTooltip.x, NuN_PinnedTooltip.y);
			else
				NuN_PinnedTooltip:ClearAllPoints();
				NuN_PinnedTooltip:SetPoint("TOP", "UIParent", "TOP", 0, -60);
			end
		end

	else
		NuN_ClearPinnedTT();
	end

	if ( ( ttType == "Contact" ) and ( NuNFrame:IsVisible() ) ) then
		if ( ( NuN_PinnedTooltip:IsVisible() ) and ( NuN_PinnedTooltip.noteName == local_player.currentNote.unit ) ) then
			NuN_CTTCheckBox:SetChecked(1);
		else
			NuN_CTTCheckBox:SetChecked(0);
		end
	elseif ( ( ttType == "General" ) and ( NuNGNoteFrame:IsVisible() ) ) then
		if ( ( NuN_PinnedTooltip:IsVisible() ) and ( NuN_PinnedTooltip.noteName == local_player.currentNote.general ) ) then
			NuN_GTTCheckBox:SetChecked(1);
		else
			NuN_GTTCheckBox:SetChecked(0);
		end
	end
end


function NuN_ClearPinnedTT()
	if ( NuN_PinnedTooltip:IsVisible() ) then
		NuN_PinnedTooltip:ClearLines();
		NuN_PinnedTooltip:Hide();
	end
end


function NuN_CheckPinnedBox(tst)
	if ( ( NuN_PinnedTooltip:IsVisible() ) and ( NuN_PinnedTooltip.noteName == tst ) ) then
		return 1;
	else
		return 0;
	end
end



function NuN_PinnedTT_OnClick()
	local ttTitle = NuN_PinnedTooltipTextLeft2:GetText();

	if ( NuN_PinnedTooltip.type == "Contact" ) then
		if ( locals.NuNDataPlayers[ttTitle] ) then
			NuN_ShowSavedNote(ttTitle);
		end

	elseif ( NuN_PinnedTooltip.type == "General" ) then
		if ( NuNData[locals.itmIndex_dbKey][ttTitle] ) then
			ttTitle = NuNData[locals.itmIndex_dbKey][ttTitle];
		end
		if ( ( NuNDataRNotes[ttTitle] ) or ( NuNDataANotes[ttTitle] ) ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
				return;
			end
			local_player.currentNote.general = ttTitle;
			NuNGNoteFrame.fromQuest = nil;
			NuN_ShowSavedGNote();
		end
	end
end


function NuN_PinnedTT_OnHide()
	NuNSettings[local_player.realmName].pT = nil;
	if ( NuN_PinnedTooltip.type == "Contact" ) then
		if ( ( NuNFrame:IsVisible() ) and ( NuN_CTTCheckBox:GetChecked() ) ) then
			NuN_CTTCheckBox:SetChecked(0);
		end

	elseif ( NuN_PinnedTooltip.type == "General" ) then
		if ( ( NuNGNoteFrame:IsVisible() ) and ( NuN_GTTCheckBox:GetChecked() ) ) then
			NuN_GTTCheckBox:SetChecked(0);
		end
	end
end


function NuN_OpenChat(noteType)
	local dspText, dspTextL;
	local hdrMax = 29;

--	UIDropDownMenu_ClearAll(NuNChatDropDown);
--	NuNTransmit:Disable();
--	NuNChatTextBox:Hide();
	NuN_ChatFrame.type = noteType;
	if ( noteType == "Contact" ) then
		dspText = local_player.currentNote.unit;
		NuN_ChatCheckBox:Show();
	elseif ( noteType == "General" ) then
		dspText = local_player.currentNote.general;
		NuN_ChatCheckBox:Hide();
	end
	if ( strfind(dspText, "|Hitem:") ) then
		hdrMax = hdrMax + 50;
	end
	dspTextL = strlen(dspText);
	if ( dspTextL > hdrMax ) then
		dspText = strsub(dspText, 1, hdrMax).."....";
	end
	NuNChatNoteTitle:SetText(dspText);
	NuN_ChatFrame:Show();

end


-- -sendC, -sendG, -sendCF, -sendGF
function NuN_ManualTransmit(formatted, tType, parms)
	if ( formatted ) then
		NuN_ChatFormatCheckBox:SetChecked(1);
	else
		NuN_ChatFormatCheckBox:SetChecked(0);
	end

	local __;
	if ( tType ) then
		__, __, noteName, sendType, tUser = strfind(parms, "\"(.*)\"%,*%s*\"(.*)\"%,*%s*\"(.*)\"");
		if ( not noteName ) then
			__, __, noteName, sendType, tUser = strfind(parms, "\"(.*)\"%,*%s*\"(.*)\"");
		end

		if ( ( noteName ) and ( sendType ) ) then
			
			-- just to be a tiny bit more forgiving
			sendType = strupper(sendType);
			
			if ( sendType == "CHANNEL" ) then
				if ( not tUser ) then
					NuN_Message("Invalid Channel");
					return;
				end
				local nTest = tonumber(tUser);
				if ( nTest ) then
					sendToChannel.id, sendToChannel.name = GetChannelName(nTest);
				else
					sendToChannel.name = tUser;
					sendToChannel.id = GetChannelName(tUser);
				end
				tUser = sendToChannel.id;
--NuN_Message( " 1 : " .. tUser );

			elseif ( sendType == "WHISPER" ) then
				if ( not tUser ) then
					NuN_Message("Invalid WHISPER Target");
					return;
				end
			end
		
			if ( ( tType == "Contact" ) and ( locals.NuNDataPlayers[noteName] ) ) then
				if ( NuNFrame:IsVisible() ) then
					NuNEditDetailsFrame:Hide();
					HideNUNFrame();
				end
				NuN_ShowSavedNote(noteName);

			elseif ( NuN_GNoteExists(noteName) ) then
				NuNGNoteFrame.fromQuest = nil;
				NuN_ShowSavedGNote();
				
			else
				NuN_Message(NUN_TRANSMISSION_MISSING .. " : " .. noteName);
				return;
			end

			locals.sendTo = sendType;

			if ( locals.sendTo ) then
				NuN_Transmit(tType, tUser);
			end

			NuNEditDetailsFrame:Hide();
			HideNUNFrame();
			NuNGNoteFrame:Hide();

		else
			NuN_Message("Invalid Parameters : " .. parms);
		end
	end
end


------------------------------------------------------------------------------------------------------------------
-- Functions for Splitting Notes and Sending them to other Players / Broadcasting to channels --
-- Main Transmit function
function NuN_Transmit(tType, tUser)
	local dfltLang = nil;
	local user = nil;
	local error = nil;
	local singleLine;
	local parsedArray = {};
	local contents = nil;
	local linesInError = {};
	local nonCriticalError = false;
	local e = 0;
	local tLog = "Transmit Log ";
	local chatTarget;
	local prfx;
	local msgPrfx = "";
	local logText = "";
	local saveLvl;
	local sendAll = true;
	local formattedNote = NuN_ChatFormatCheckBox:GetChecked();
	local msgDelay = NuN_DTrans.tDelay;

	-- ensure valid delay value, otherwise reset the value and the edit box displayed value
	if ( NuN_ChatDelay_EditBoxValidate() ) then
		NuN_ChatDelay_EditBoxInit();
	end

	NuNTransmit:Disable();
	NuN_transmissionTimer = defaultReceiptDeadline;
	busySending.active = true;

	locals.msgSeq = locals.msgSeq + 1;
	if ( locals.msgSeq > 9 ) then
		locals.msgSeq = 1;
	end
	local errorNoteName = locals.player_Name.."_X"..msgSeq;
	NuN_msgKey = locals.player_Name..msgSeq..":";
	tLog = tLog..NuN_msgKey;

	if ( formattedNote ) then
		msgPrfx = NuN_msgKey;
	end

	if ( NuNSettings[local_player.realmName].dLevel ) then
		NuNDataANotes[tLog] = {};
		logText = NuNF.NuN_GetDateStamp().."\n";
		NuNDataANotes[tLog].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		saveLvl = "Account";
	else
		NuNDataRNotes[tLog] = {};
		logText = NuNF.NuN_GetDateStamp().."\n";
		NuNDataRNotes[tLog].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		saveLvl = "Realm";
	end

	if ( NuNChatTextBox:IsVisible() ) then
		user = NuNChatTextBox:GetText();
		busySending.user = user;
	elseif ( tUser ) and ( locals.sendTo == "WHISPER" ) then						-- 5.61
		user = tUser;														-- 5.61
		busySending.user = user;											-- 5.61
	end

	if ( locals.sendTo == "WHISPER" ) or ( locals.sendTo == "NuN" ) then
		chatTarget = user;
	elseif ( locals.sendTo == "CHANNEL" ) then
--		msgDelay = NuN_DTrans.tDelay;
		if ( ( not sendToChannel.id ) or ( ( type(sendToChannel.id) == "number" ) and ( sendToChannel.id < 1 ) ) ) then
			NuN_Message("Invalid id");
			return;
		else
			local cIndex, cName = GetChannelName(sendToChannel.id);
			if ( ( ( type(sendToChannel.id) == "number" ) and ( not cName ) ) or ( not cIndex ) ) then
				NuN_Message("Invalid name"..sendToChannel.name.." : "..sendToChannel.id);
				return;
			else
				user = sendToChannel.id;
				chatTarget = sendToChannel.name;
			end
		end
	else
		chatTarget = locals.sendTo;
		if ( ( locals.sendTo == "GUILD" ) or ( locals.sendTo == "OFFICER" ) ) then
			if ( not GetGuildInfo("player") ) then
				sendAll = nil;
			end
		elseif ( ( locals.sendTo == "RAID" ) or ( locals.sendTo == "RAID_WARNING" ) ) then
			if ( not UnitInRaid("player") ) then
				sendAll = nil;
			end
		elseif ( locals.sendTo == "PARTY" ) then
			if ( ( not GetNumPartyMembers() ) or ( GetNumPartyMembers() < 1 ) ) then
				sendAll = nil;
			end
		end
	end
	
	prfx = NuN_msgKey..NUN_TRANSMISSION_PREFIX1..locals.player_Name.."  --->  "..chatTarget.." ::"..msgDelay.."::";
	prfx2 = NuN_msgKey..NuNC.NUN_SOURCE..NUN_CLIENT.." v"..NUN_VERSION;
	logText = logText.."\n"..prfx;

	-- Which type of note to create
	if ( tType == "Contact" ) then										-- 5.61
		parsedArray, error = NuN_TransmitContact(dfltLang, user);		-- 5.61
	elseif ( tType == "General" ) then									-- 5.61
		parsedArray, error = NuN_TransmitGeneral(dfltLang, user);		-- 5.61
	elseif ( NuN_ChatFrame.type == "Contact" ) then						-- 5.61
		parsedArray, error = NuN_TransmitContact(dfltLang, user);
	else
		parsedArray, error = NuN_TransmitGeneral(dfltLang, user);
	end

	if ( parsedArray ) then
		local tmp_c_note = local_player.currentNote.general;
		local tmp_g_text = general.text;
		contents = getn(parsedArray);
		local lineCount = 0;
		local flagged = nil;
		for i=1, contents, 1 do
			singleLine = parsedArray[i];
			singleLine = strgsub(singleLine, "\n", " ");
			singleLine = strgsub(singleLine, "\\n", " ");
			if ( strlen(singleLine) > 255 ) then
				error = "Err02";
				break;
			end
			local beginnings = NuN_Validate(singleLine, "|c");
			local endings = NuN_Validate(singleLine, "|h|r");
			if ( beginnings == endings ) then
				parsedArray[i] = singleLine;
				lineCount = lineCount + 1;
				if ( strlen(logText) < (NuNC.NUN_MAX_TXT_LIM - 360) ) then
					logText = logText.."\n"..singleLine;
				elseif ( not flagged ) then
					flagged = true;
					logText = logText.."\n"..NUN_NOROOM;
				end

			else
				parsedArray[i] = "";
				nonCriticalError = true;
				e = e + 1;
				linesInError[e] = NUN_LINEERROR_TEXT.."----->"..i.." : \n";
				e = e + 1;
				linesInError[e] = singleLine.."\n";
			end
		end
		local_player.currentNote.general = tLog;
		general.text = logText;
		NuNF.NuN_SetGText(saveLvl);
		local_player.currentNote.general = tmp_c_note;
		general.text = tmp_g_text;
	end

	local dCount = 0;

	if ( contents ) and ( not error ) then
		if ( locals.sendTo == "SELF" ) then
			DEFAULT_CHAT_FRAME:AddMessage(prfx);
		elseif ( ( locals.sendTo == "CHANNEL" ) and ( not formattedNote ) ) then

		else
			if ( msgDelay == 0 ) then
				SendChatMessage(prfx, locals.sendTo, dfltLang, user);
			else
				dCount = dCount + 1;
				NuN_DTrans.pArray[ dCount ] = prfx;
			end
		end
		if ( sendAll ) then
			if ( formattedNote ) then
				NuN_Message(NUN_SENDING_NOTE.." -> "..chatTarget);
				if ( locals.sendTo == "SELF" ) then
					DEFAULT_CHAT_FRAME:AddMessage(prfx2);
				else
					if ( msgDelay == 0 ) then
						SendChatMessage(prfx2, locals.sendTo, dfltLang, user);
					else
						dCount = dCount + 1;
						NuN_DTrans.pArray[ dCount ] = prfx2;
					end
				end
			end
			for i=1, contents, 1 do
				if ( locals.sendTo == "SELF" ) then
					DEFAULT_CHAT_FRAME:AddMessage(msgPrfx..parsedArray[i]);
				else
					if ( msgDelay == 0 ) then
						SendChatMessage(msgPrfx..parsedArray[i], locals.sendTo, dfltLang, user);
					else
						dCount = dCount + 1;
						NuN_DTrans.pArray[ dCount ] = msgPrfx..parsedArray[i];
					end
				end
			end
		end
	elseif ( error ) then
		NuN_Message(NUN_TRANSMISSION_ERROR..error);
	else
		NuN_Message(NUN_TRANSMISSION_MISSING);
	end

	if ( dCount > 0 ) then
		NuN_DTrans.Status = "Sending";
		NuN_DTrans.Params.sendTo = locals.sendTo;
		NuN_DTrans.Params.dfltLang = dfltLang;
		NuN_DTrans.Params.user = user;
		NuN_DTrans.tTrack = 0;
		NuN_transmissionTimer = defaultReceiptDeadline + ( msgDelay * dCount );
	end

	if ( nonCriticalError ) then
		if ( NuNSettings[local_player.realmName].dLevel ) then
			NuNDataANotes[errorNoteName] = {};
			NuNDataANotes[errorNoteName].txt = "";
			NuNDataANotes[errorNoteName].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		else
			NuNDataRNotes[errorNoteName] = {};
			NuNDataRNotes[errorNoteName].txt = "";
			NuNDataANotes[errorNoteName].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		end
		for i=1, getn(linesInError), 1 do					-- #linesInError
			local lineTxt = NuNF.NuN_SetSaveText( linesInError[i].."\n" );
			if ( NuNSettings[local_player.realmName].dLevel ) then
				NuNDataANotes[errorNoteName].txt = NuNDataANotes[errorNoteName].txt..lineTxt;
			else
				NuNDataRNotes[errorNoteName].txt = NuNDataRNotes[errorNoteName].txt..lineTxt;
			end
		end
		NuN_Message(NUN_NONCRITICAL_ERROR..errorNoteName);
	end
end



function NuN_TransmitContact(dfltLang, user)
	local parsedArray = {};
	local singleLine = "";
	local lineType = "";
	local formatIt = NuN_ChatFormatCheckBox:GetChecked();


	if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
		local parsedArray = {};
		local singleLine = "";
		local arrayCounter = 0;
		local parseText = "";
		local txtArray = {};

		if ( formatIt ) then
			arrayCounter = arrayCounter + 1;
			parsedArray[arrayCounter] = NUN_TRANSMISSION_PREFIX2.."Contact : "..local_player.currentNote.unit;
--		else
--			parsedArray[arrayCounter] = local_player.currentNote.unit;
		end

		if ( not NuN_ChatCheckBox:GetChecked() ) then
			local generalCode = "<";
			singleLine = locals.NuNDataPlayers[local_player.currentNote.unit].faction;
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].race ) then
				if ( NuN_horde ) then
					singleLine = singleLine..",  "..NUN_HRACES[locals.NuNDataPlayers[local_player.currentNote.unit].race];
				else
					singleLine = singleLine..",  "..NUN_ARACES[locals.NuNDataPlayers[local_player.currentNote.unit].race];
				end
				generalCode = generalCode..locals.NuNDataPlayers[local_player.currentNote.unit].race;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode.."-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].cls ) then
				if ( NuN_horde ) then
					singleLine = singleLine..",  "..NUN_HCLASSES[locals.NuNDataPlayers[local_player.currentNote.unit].cls];
				else
					singleLine = singleLine..",  "..NUN_ACLASSES[locals.NuNDataPlayers[local_player.currentNote.unit].cls];
				end
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].cls;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].sex ) then
				singleLine = singleLine..",  "..NUN_SEXES[locals.NuNDataPlayers[local_player.currentNote.unit].sex];
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].sex;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].prating ) then
				singleLine = singleLine..", "..NuNSettings.ratings[locals.NuNDataPlayers[local_player.currentNote.unit].prating];
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].prating;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].prof1 ) then
				singleLine = singleLine..",  "..NUN_PROFESSIONS[locals.NuNDataPlayers[local_player.currentNote.unit].prof1];
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].prof1;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].prof2 ) then
				singleLine = singleLine..",  "..NUN_PROFESSIONS[locals.NuNDataPlayers[local_player.currentNote.unit].prof2];
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].prof2;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].arena ) then
				singleLine = singleLine..", "..NUN_ARENAR[locals.NuNDataPlayers[local_player.currentNote.unit].arena];
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].arena;
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",-";
			end
			if ( locals.NuNDataPlayers[local_player.currentNote.unit].hrank ) then
				if ( NuN_horde ) then
					singleLine = singleLine..",  "..NUN_HRANKS[locals.NuNDataPlayers[local_player.currentNote.unit].hrank];
				else
					singleLine = singleLine..",  "..NUN_ARANKS[locals.NuNDataPlayers[local_player.currentNote.unit].hrank];
				end
				generalCode = generalCode..","..locals.NuNDataPlayers[local_player.currentNote.unit].hrank..">";
			elseif ( formatIt ) then
				singleLine = singleLine..", -";
				generalCode = generalCode..",->";
			end

			if ( singleLine ~= "" ) then
				if ( formatIt ) then
					lineType = "G:";
				end
				singleLine = lineType..singleLine;
				if ( formatIt ) then
					singleLine = singleLine.." "..generalCode;
				end
				arrayCounter = arrayCounter + 1;
				parsedArray[arrayCounter] = singleLine;
			end

			if ( formatIt ) then
				lineType = "U:";
			end
			for n = 1, locals.uBttns, 1 do
				singleLine = "";
				locals.headingNumber = locals.pHead..n;
				locals.headingName = local_player.currentNote.unit .. locals.headingNumber;
				locals.headingDate = local_player.currentNote.unit .. locals.pDetl..n;
				if ( ( locals.NuNDataPlayers[locals.headingName] ) and ( locals.NuNDataPlayers[locals.headingName].txt ) ) then
					singleLine = locals.NuNDataPlayers[locals.headingName].txt.."~    ";
				elseif ( NuNSettings[local_player.realmName][locals.headingNumber] ) then
					singleLine = NuNSettings[local_player.realmName][locals.headingNumber].txt.."~    ";
				else
					singleLine = NUN_DFLTHEADINGS[n].."~    ";
				end
				if ( ( locals.NuNDataPlayers[locals.headingDate] ) and ( locals.NuNDataPlayers[locals.headingDate].txt ) ) then
					singleLine = singleLine.."~"..locals.NuNDataPlayers[locals.headingDate].txt;
				end
				if ( singleLine ~= "" ) then
					arrayCounter = arrayCounter + 1;
					singleLine = lineType..n..":"..singleLine;
					parsedArray[arrayCounter] = singleLine;
				end
			end
		end

--		parseText = NuNF.NuN_GetCText(local_player.currentNote.unit);
		parseText = NuNText:GetText();
		txtArray = NuN_ParseNote(parseText);
		if ( txtArray ) then
			local lKey = 0;
			for i=1, getn(txtArray), 1 do					-- #txtArray
				if ( formatIt ) then
					lKey = lKey + 1;
					if ( lKey > 9 ) then
						lKey = 1;
					end
					lineType = "T:"..lKey;
				end
				arrayCounter = arrayCounter + 1;
				parsedArray[arrayCounter] = lineType..txtArray[i];
			end
		end

		if ( formatIt ) then
			arrayCounter = arrayCounter + 1;
			parsedArray[arrayCounter] = NUN_TRANSMISSION_POSTFIX.."Contact : "..local_player.currentNote.unit;
		end

		return parsedArray, nil;
	else
		return nil, "Err01";
	end
end



function NuN_TransmitGeneral(dfltLang, user)
	local ref = local_player.currentNote.general;
	local lineType = "";
	local formatIt = NuN_ChatFormatCheckBox:GetChecked();

	if ( NuNData[locals.itmIndex_dbKey][ref] ) then
		ref = NuNData[locals.itmIndex_dbKey][ref];
	end

	if ( ( NuNDataRNotes[ref] ) or ( NuNDataANotes[ref] ) ) then
		local parsedArray = {};
		local singleLine = "";
		local arrayCounter = 0;
		local parseText = "";
		local txtArray = {};
		local nType;

		if ( ( NuNDataRNotes[ref] ) and ( NuNDataRNotes[ref].type ) ) then
			nType = NuNDataRNotes[ref].type;
		elseif ( ( NuNDataANotes[ref] ) and ( NuNDataANotes[ref].type ) ) then
			nType = NuNDataANotes[ref].type;
		else
			nType = 1;
		end

		if ( not formatIt ) then
			nType = NUN_NOTETYPES[nType].Display;
			if ( nType == "   " ) then
				nType = "-";
			end
		end

		if ( formatIt ) then
			arrayCounter = arrayCounter + 1;
			parsedArray[arrayCounter] = NUN_TRANSMISSION_PREFIX2.."General("..nType..") : "..local_player.currentNote.general;
--		else
--			parsedArray[arrayCounter] = local_player.currentNote.general;
		end

--		parseText = NuNF.NuN_GetGText(local_player.currentNote.general);
		parseText = NuNGNoteTextScroll:GetText();
		txtArray = NuN_ParseNote(parseText);
		if ( txtArray ) then
			local lKey = 0;
			for i=1, getn(txtArray), 1 do				-- #txtArray
				if ( formatIt ) then
					lKey = lKey + 1;
					if ( lKey > 9 ) then
						lKey = 1;
					end
					lineType = "T:"..lKey;
				end
				arrayCounter = arrayCounter + 1;
				parsedArray[arrayCounter] = lineType..txtArray[i];
			end
		end

		if ( formatIt ) then
			arrayCounter = arrayCounter + 1;
			parsedArray[arrayCounter] = NUN_TRANSMISSION_POSTFIX.."General : "..local_player.currentNote.general;
		end

		return parsedArray, nil;
	else
		return nil, "Err01";
	end
end



function NuN_ParseNote(parseText)
	local parsedArray = {};
	local arrayCounter = 0;
	local p1 = 0;
	local p2 = 0;
	local txtTmp = "";

	if ( ( not parseText ) or ( parseText == "" ) or ( parseText == "\n" ) or ( parseText == " " ) ) then
		return nil;
	end

	parseText = NuN_RemoveColours(parseText);

	if ( ( not parseText ) or ( parseText == "" ) or ( parseText == "\n" ) or ( parseText == " " ) ) then
		return nil;
	end

	parseText = NuN_CheckHyperlinkPositions(parseText);
	parseText = NuN_CheckEnchantLinkPositions(parseText);

	local parseTextLen = strlen(parseText);
	local negOffset = -1 * (parseTextLen);

	parseText = gsub(parseText, "|h|r\n", "|h|r");	-- Not EVERY Hyperlink is NECESSARILY followed by a SINGLE new line...
	parseText = gsub(parseText, "|h|r", "|h|r\n");	-- Until AFTER BOTH of these lines of code

	while ( negOffset < 0 ) do
		p2 = strfind(parseText, "\n", (p1+1) );
		if ( ( p2 == nil ) or ( p2 > ( NuNC.NUN_CHAT_LIMIT + p1 ) ) ) then
			negOffset = p1 + NuNC.NUN_CHAT_LIMIT - parseTextLen;
			if ( negOffset < 0 ) then
				p2 = strfind(parseText, " ", negOffset);
				if ( ( p2 == nil ) or ( p2 <= p1 ) ) then
					txtTmp = strsub(parseText, (p1+1), (p1+NuNC.NUN_CHAT_LIMIT));
				else
					txtTmp = strsub(parseText, (p1+1), p2);
				end
			else
				txtTmp = strsub(parseText, (p1+1));
			end
		else
			txtTmp = strsub(parseText, (p1+1), p2);
		end
		if ( p2 ) then
			p1 = p2;
		else
			p1 = parseTextLen;
		end
		if ( txtTmp ~= "" ) and ( txtTmp ~= " " ) then
			arrayCounter = arrayCounter + 1;
			parsedArray[arrayCounter] = txtTmp;
		end
	end

	return parsedArray;
end


function NuN_RemoveColours(colouredText)
	local plainText, preText, postText, midText = "", "", "", "";
	local workingText = strgsub(colouredText, "<", "¬1~");
	local workingText = strgsub(workingText, ">", "¬2~");

	local next = 0;
	while ( true ) do
		local ps = strfind(workingText, "|c%x%x%x%x%x%x%x%x", next+1);
		if ( ps ) then
			if ( strsub(workingText, ps+10, ps+16) == "|Hitem:" ) then
			elseif ( strsub(workingText, ps+10, ps+19) == "|Henchant:" ) then
			elseif ( strsub(workingText, ps+10, ps+17) == "|Hspell:" ) then
			else
				if ( strsub(workingText, ps+10, ps+11) == "|h" ) then
					toEnd = 2;
				else
					toEnd = 0;
				end
				if ( ps == 1 ) then
					preText = "";
				else
					preText = strsub(workingText, 1, ps-1);
				end
				midText = strsub(workingText, ps, (ps+9+toEnd));
				postText = strsub(workingText, ps+10+toEnd);
				workingText = preText .. "<" .. midText .. ">" .. postText;
				ps = ps + 2;
			end
			next = ps+8;
		else
			break;
		end
	end

	local next = 0;
	while ( true ) do
		local ps = strfind(workingText, "|r", next+1);
		if ( ps ) then
			if ( strsub(workingText, ps-3, ps-1) == "\]|h" ) then
			else
				if ( strsub(workingText, ps-2, ps-1) == "|h" ) then
					fromStart = -2;
				else
					fromStart = 0;
				end
				if ( ps == 1 ) then
					preText = "";
				else
					preText = strsub(workingText, 1, (ps-1+fromStart));
				end
				midText = strsub(workingText, (ps+fromStart), ps+1);
				postText = strsub(workingText, ps+2);
				workingText = preText .. "<" .. midText .. ">" .. postText;
				ps = ps + 2;
			end
			next = ps;
		else
			break;
		end
	end
	workingText = strgsub(workingText, "%b<>", "");

	plainText = strgsub(workingText, "¬1~", "<");
	plainText = strgsub(plainText, "¬2~", ">");

	return plainText;
end


function NuN_Validate(txt, tst)
	local p1 = 0;
	local p2 = 0;
	local count = 0;

	while ( true ) do
		p2 = strfind(txt, tst, (p1+1));
		if ( p2 == nil ) then
			break;
		else
			count = count + 1;
			p1 = p2;
		end
	end

	return count;
end

function NuN_ChatFormatCheckBox_OnClick()
	if ( NuN_ChatFormatCheckBox:GetChecked() ) then
		NuN_Message(NUN_TRANSMISSION_WARNING);
		PlaySound("igMainMenuOptionCheckBoxOn");
	else
		PlaySound("igMainMenuOptionCheckBoxOff");
	end
end

function NuN_CheckHyperlinkPositions(theText)
	local hypBegs = {};
	local p1 = 0;
	local p2 = 0;
	local counter = 0;
	local Begs = 0;
	local rtrnText = "";

	while ( true ) do
		p2 = strfind(theText, "|Hitem:", (p1+1) );
		if ( p2 == nil ) then
			break;
		else
			counter = counter + 1;
			hypBegs[counter] = p2 - 10;
			p1 = p2;
		end
	end

	if ( hypBegs ) then
		local base = 1;
		local preText = "";
		local len = 0;
		for i=1, getn(hypBegs), 1 do						-- #hypBegs
			preText = strsub(theText, base, (hypBegs[i] - 1));
			len = strlen(preText);
			local p3 = 0;
			local p4 = 0;
			p2 = nil;
			while ( true ) do
				p4 = strfind(preText, "\n", (p3+1));
				if ( p4 == nil ) then
					break;
				else
					p2 = p4;
					p3 = p4;
				end
			end
			if ( p2 == nil ) and ( len > 120 ) then
				rtrnText = rtrnText..preText.."\n";
				base = base + len;
			elseif ( p2 ) and ( (len - p2) > 120 ) then
				rtrnText = rtrnText..preText.."\n";
				base = base + len;
			elseif ( p2 ) then
				rtrnText = rtrnText..strsub(theText, base, (base + p2));
				base = base + p2 + 1;
			end
		end
		rtrnText = rtrnText..strsub(theText, base);
	end

	return rtrnText;
end

function NuN_CheckEnchantLinkPositions(theText)
	local hypBegs = {};
	local p1 = 0;
	local p2 = 0;
	local counter = 0;
	local Begs = 0;
	local rtrnText = "";

	while ( true ) do
		p2 = strfind(theText, "|Henchant:", (p1+1) );
		if ( p2 == nil ) then
			break;
		else
			counter = counter + 1;
			hypBegs[counter] = p2 - 10;
			p1 = p2;
		end
	end

	if ( hypBegs ) then
		local base = 1;
		local preText = "";
		local len = 0;
		for i=1, getn(hypBegs), 1 do						-- #hypBegs
			preText = strsub(theText, base, (hypBegs[i] - 1));
			len = strlen(preText);
			local p3 = 0;
			local p4 = 0;
			p2 = nil;
			while ( true ) do
				p4 = strfind(preText, "\n", (p3+1));
				if ( p4 == nil ) then
					break;
				else
					p2 = p4;
					p3 = p4;
				end
			end
			if ( p2 == nil ) and ( len > 120 ) then
				rtrnText = rtrnText..preText.."\n";
				base = base + len;
			elseif ( p2 ) and ( (len - p2) > 120 ) then
				rtrnText = rtrnText..preText.."\n";
				base = base + len;
			elseif ( p2 ) then
				rtrnText = rtrnText..strsub(theText, base, (base + p2));
				base = base + p2 + 1;
			end
		end
		rtrnText = rtrnText..strsub(theText, base);
	end

	return rtrnText;
end

-- Functions for Splitting and Sending Notes to other Players --
---------------------------------------------------------------------------------


function NuN_NPCTarget()
	local chkName = UnitName("target");
	local npcText = "";

	if ( ( chkName ) and ( not UnitPlayerControlled("target") ) ) then
		NPCInfo_Proceed = nil;
		NuN_NPCInfo(NuN_NPCGetText);
	end
end


function NuN_NPCGetText()
	local npcText = NuN_NPCInfo();
	NuNGNoteTextScroll:SetText( NuNGNoteTextScroll:GetText().."\n"..npcText );
	GameTooltip:ClearLines();
	GameTooltip:Hide();
end



function NuNScaleFrameShow()
	if ( NuN_ScaleFrame:IsVisible() ) then
		NuN_ScaleFrame:Hide();
	else
		NuN_ScaleFrame:Show();
	end
end


function NuNFrameScaleSlider_OnShow(self)
	local pScale = NuNSettings[local_player.realmName].pScale;

	NuNFrameScaleSliderLow:SetText(NuNC.NUN_FRAMESCALE_MAX_TXT);
	NuNFrameScaleSliderHigh:SetText(NuNC.NUN_FRAMESCALE_MIN_TXT);

	NuNFrameScaleSliderCurrent:SetText( strformat("%d", (pScale * 100)) .. "%");
	self:SetMinMaxValues(NuNC.NUN_FRAMESCALE_MIN, NuNC.NUN_FRAMESCALE_MAX);
	self:SetValueStep(NuNC.NUN_FRAMESCALE_STEP);
	self:SetValue(NuNC.NUN_FRAMESCALE_MIN + NuNC.NUN_FRAMESCALE_MAX - pScale);
	self.previousValue = self:GetValue();
end


function NuNFrameScaleSlider_OnValueChanged(self,value)
	local pScale;

	if (self:GetValue() ~= self.previousValue) then
		self.previousValue = self:GetValue();
		pScale = (NuNC.NUN_FRAMESCALE_MIN + NuNC.NUN_FRAMESCALE_MAX - self:GetValue());
		NuNSettings[local_player.realmName].pScale = pScale;
		NuNFrameScaleSliderCurrent:SetText( strformat("%d", (pScale * 100)) .. "%");
		if ( NuNOptionsFrame:IsVisible() ) then
			NuNOptionsFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		end
		if ( NuNFrame:IsVisible() ) then
			NuNFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		end
		if ( NuNGNoteFrame:IsVisible() ) then
			NuNGNoteFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		end
		if ( NuNSearchFrame:IsVisible() ) then
			NuNSearchFrame:SetScale(NuNSettings[local_player.realmName].pScale);
		end
	end
end


function NuNFontScaleSlider_OnShow(self)
	local tScale = NuNSettings[local_player.realmName].tScale;

	NuNFontScaleSliderLow:SetText(NuNC.NUN_TT_FONTSCALE_MAX_TXT);
	NuNFontScaleSliderHigh:SetText(NuNC.NUN_TT_FONTSCALE_MIN_TXT);

	NuNFontScaleSliderCurrent:SetText( strformat("%d", (tScale * 100)) .. "%");
	self:SetMinMaxValues(NuNC.NUN_TT_FONTSCALE_MIN, NuNC.NUN_TT_FONTSCALE_MAX);
	self:SetValueStep(0.01);	-- 5.60 replaced constant (note NuNC.NUN_FONT deleted also)
	self:SetValue(NuNC.NUN_TT_FONTSCALE_MIN + NuNC.NUN_TT_FONTSCALE_MAX - tScale);
	self.previousValue = self:GetValue();
end


function NuNFontScaleSlider_OnValueChanged(self,value)
	local tScale;

	if (self:GetValue() ~= self.previousValue) then
		self.previousValue = self:GetValue();
		tScale = (NuNC.NUN_TT_FONTSCALE_MIN + NuNC.NUN_TT_FONTSCALE_MAX - self:GetValue());
		NuNSettings[local_player.realmName].tScale = tScale;
		NuNFontScaleSliderCurrent:SetText( strformat("%d", (tScale * 100)) .. "%");
		NuN_PinnedTooltip:SetScale(NuNSettings[local_player.realmName].tScale);
		NuN_Tooltip:SetScale(NuNSettings[local_player.realmName].tScale);
	end
end



function NuNMapFontScaleSlider_OnShow(self)
	local mScale = NuNSettings[local_player.realmName].mScale;

	NuNMapFontScaleSliderLow:SetText(NuNC.NUN_TT_MAPFONTSCALE_MAX_TXT);
	NuNMapFontScaleSliderHigh:SetText(NuNC.NUN_TT_MAPFONTSCALE_MIN_TXT);

	NuNMapFontScaleSliderCurrent:SetText( strformat("%d", (mScale * 100)) .. "%");
	self:SetMinMaxValues(NuNC.NUN_TT_MAPFONTSCALE_MIN, NuNC.NUN_TT_MAPFONTSCALE_MAX);
	self:SetValueStep(0.01);	-- 5.60 replaced constant
	self:SetValue(NuNC.NUN_TT_MAPFONTSCALE_MIN + NuNC.NUN_TT_MAPFONTSCALE_MAX - mScale);
	self.previousValue = self:GetValue();
end


function NuNMapFontScaleSlider_OnValueChanged(self,value)
	local mScale;

	if (self:GetValue() ~= self.previousValue) then
		self.previousValue = self:GetValue();
		mScale = (NuNC.NUN_TT_MAPFONTSCALE_MIN + NuNC.NUN_TT_MAPFONTSCALE_MAX - self:GetValue());
		NuNSettings[local_player.realmName].mScale = mScale;
		NuNMapFontScaleSliderCurrent:SetText( strformat("%d", (mScale * 100)) .. "%");
		mScale = UIParent:GetScale() * NuNSettings[local_player.realmName].mScale;
		WorldMapTooltip:SetScale(NuNSettings[local_player.realmName].mScale);
		NuN_MapTooltip:SetScale(NuNSettings[local_player.realmName].mScale);
		NuNPopup:SetScale(NuNSettings[local_player.realmName].mScale);
	end
end


-- Mutliple functions for monitoring and noting Quest information

function NuN_ForceUpdateQuestNotes(qEvent)
	if ( NuNSettings[local_player.realmName].autoQ ) then
		NuNF.NuN_UpdateQuestNotes(qEvent);
	end
end



function NuN_OptionsTextLengthSet(self)
	local len = self:GetText();

	if ( len ) then
		NuNSettings[local_player.realmName].ttLen = len;
	else
		NuNSettings[local_player.realmName].ttLen = "0";
	end
end



function NuN_OptionsTextLineLengthSet(self)
	local len = self:GetText();

	if ( len ) then
		NuNSettings[local_player.realmName].ttLLen = len;
	else
		NuNSettings[local_player.realmName].ttLLen = "0";
	end
end


-- the list of Quests someone has had, together with their status, and when they Accepted, completed, handed in, abandoned them
function NuN_FetchQuestHistory()
	local idx, value;
	local counter = 0;
	local results = 0;

	locals.foundNuN = {};
	for idx, value in pairs(NuNQuestHistory) do
		counter = counter + 1;
		locals.foundNuN[counter] = NuNC.NUN_QUEST_C.index;
	end

	tsort(locals.foundNuN, NuNF.NuN_SortQuestHistory);
	results = getn(locals.foundNuN);
	NuNSearchTitleText:SetText(locals.player_Name.."'s "..NUN_QUESTS_TEXT.." ("..results..")");

	UIDropDownMenu_SetSelectedID(NuNSearchQHDropDown, locals.questHistory.Index);
	UIDropDownMenu_SetText(NuNSearchQHDropDown, locals.questHistory.Title);
	NuNSearchQHDropDown:Show();
	NuNSearchFrame_MassDelete:Hide();
	NuNSearchFrame_Export:Hide();
	NuNExtraOptions:Hide();
	NuNSearchFrameSearchButton:Disable();
	NuNSearchClassDropDown:Hide();
	NuNSearchProfDropDown:Hide();
	NuNSearchTextBox:Hide();
	NuNSearchSubSet:Hide();
	NuNSearchFrame.qh = true;

	NuNSearch_Update();
	if ( ( locals.deletedE ) and ( locals.visibles > 0 ) and ( locals.lastBttn ~= nil ) ) then
		locals.deletedE = false;
		if ( locals.lastBttnIndex > locals.visibles ) then
			NuNSearch_HighlightRefresh(locals.lastVisible);
			NuNSearchNote_OnClick(locals.lastVisible);
		else
			NuNSearch_HighlightRefresh(locals.lastBttn);
			NuNSearchNote_OnClick(locals.lastBttn);
		end
	else
		NuNSearch_HighlightRefresh(nil);
	end
	if ( NuNGNoteFrame:IsVisible() ) then
		NuNGNoteFrame.fromQuest = local_player.currentNote.general;
	end
end

function NuN_ToggleMicroButtons()
	if ( NuNMicroFrame:IsVisible() ) then
		NuNMicroFrame:Hide();
		NuNSettings[local_player.realmName].hideMicro = "1";
	else
		NuNMicroFrame:Show();
		NuNSettings[local_player.realmName].hideMicro = nil;
	end
end


-- autonote Party members, and count the number of times you have partied with them
function NuN_ProcessParty()
	local pChanged = nil;
	local lMember, idx, value;
	local lGroupType;
	local partyA = {};
	local lMembers;
	local inInstance, location = IsInInstance();
	
	if (UnitInRaid("player")) then
		lGroupType = "raid";
		lMembers = GetNumRaidMembers();
	else
		lGroupType = "party";
		lMembers = GetNumPartyMembers();
	end
	
	-- Need to update this check with an IsInInstance check..... !!!
	if ( ( inInstance ) and ( ( location == "pvp" ) or ( location == "arena" ) ) ) then
		return;					-- Simply don't process in BG where many players from other Realms
	end

	if ( not NuNData[local_player.realmName][NuNC.NUN_PARTIES] ) then
		NuNData[local_player.realmName][NuNC.NUN_PARTIES] = {};
	end
	if ( ( not NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name] ) or ( ( not UnitInRaid("player") ) and ( lMembers == 0 ) ) ) then
		NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name] = {};
	end
	
	for i = 1, lMembers, 1 do
		lUnit = lGroupType..i;
		lMember = UnitName(lUnit);
		if ( ( lMember == UNKNOWN ) or ( lMember == UNKNOWNOBJECT ) ) then		-- 5.60 "Unknown Entity"
			return;
		end
		if ( lMember ) then
			partyA[lMember] = {};
			partyA[lMember].pos = i;
			if ( not NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name][lMember] ) then
				NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name][lMember] = {};
				NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name][lMember].pos = i;
				if ( not locals.NuNDataPlayers[lMember] ) then
					locals.NuNDataPlayers[lMember] = {};
					locals.NuNDataPlayers[lMember].type = NuNC.NUN_PARTY_C;
					locals.NuNDataPlayers[lMember].faction = local_player.factionName;
					locals.NuNDataPlayers[lMember][locals.txtTxt] = NUN_AUTO_PARTIED..NuNF.NuN_GetDateStamp();
					locals.NuNDataPlayers[lMember][locals.player_Name] = {};
					locals.NuNDataPlayers[lMember][locals.player_Name].partied = 1;
				else
					if ( not locals.NuNDataPlayers[lMember][locals.player_Name] ) then
						locals.NuNDataPlayers[lMember][locals.player_Name] = {};
						locals.NuNDataPlayers[lMember][locals.player_Name].partied = 1;
					elseif ( not locals.NuNDataPlayers[lMember][locals.player_Name].partied ) then
						locals.NuNDataPlayers[lMember][locals.player_Name].partied = 1;
					else
						locals.NuNDataPlayers[lMember][locals.player_Name].partied = locals.NuNDataPlayers[lMember][locals.player_Name].partied + 1;
						----ToDo: Add Option Setting to Enable/Disable this feature (Record every party join time)
						--if (locals.NuNDataPlayers[lMember][locals.txtTxt] == nil) then
						--	locals.NuNDataPlayers[lMember][locals.txtTxt] = NUN_AUTO_PARTIED..NuNF.NuN_GetDateStamp();
						--else
						--	locals.NuNDataPlayers[lMember][locals.txtTxt] = locals.NuNDataPlayers[lMember][locals.txtTxt].."?n"..NUN_AUTO_PARTIED..NuNF.NuN_GetDateStamp();
						--end
					end
				end
				locals.NuNDataPlayers[lMember][locals.player_Name].partiedOn = NuNF.NuN_GetDateStamp();
				NuNF.NuN_UnitInfoDB(lMember, lUnit);												-- 5.60 Auto-populate some data 
			end
		end
	end

	for idx, value in pairs(NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name]) do
		if ( not partyA[idx] ) then
			if ( UnitInRaid("player") ) then
				local rID = NuNF.NuN_CheckRaidByName(idx);
				if ( not rID ) then
					NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name][idx] = nil;
				end
			else
				NuNData[local_player.realmName][NuNC.NUN_PARTIES][locals.player_Name][idx] = nil;
			end
		end
	end
end



function NuN_PartyDownButton_OnClick()
	local lParties = locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partied;

	lParties = lParties - 1;
	if ( lParties < 1 ) then
		locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partied = nil;
		locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partiedOn = nil;
--		NuNPartiedLabel:Hide();
		NuNPartiedNumberLabel:SetText("(0)");
		NuNPartiedNumberLabel:Hide();
		NuNFramePartyDownButton:Hide();
	else
		locals.NuNDataPlayers[local_player.currentNote.unit][locals.player_Name].partied = lParties;
		NuNPartiedNumberLabel:SetText("(x"..tostring(lParties)..")");
	end
end



-- When Noting a Merchant / Vendor, then NotesUNeed can note what they sell also
function NuN_BuildShoppingList()
	local listText = "";
	local mName = MerchantNameText:GetText();

	if ( mName == local_player.currentNote.general ) then
		local iPrice, iPriceTxt, iQuant, iNumAvail, iLink, iDiscard;
		local numMerchantItems = GetMerchantNumItems();

		for i=1, numMerchantItems, 1 do
			iLink = GetMerchantItemLink(i);
			if ( iLink ) then
				listText = listText.."\n"..iLink;
			end
			iDiscard, iDiscard, iPrice, iQuant, iNumAvail = GetMerchantItemInfo(i);
			if ( ( iQuant ) and ( iQuant > 1 ) ) then
				listText = listText.." ("..iQuant..") ";
			end
			if ( ( iPrice ) and ( iPrice > 0 ) ) then
				iPriceTxt = NuN_BuildMoneyString(iPrice);
				listText = listText.."   "..iPriceTxt;
			end
			if ( ( iNumAvail ) and ( iNumAvail > 0 ) ) then
				listText = listText.."        "..NUN_LIMITED;
			end
		end
	end

	if ( listText == "" ) then
		return nil;
	else
		return listText;
	end
end


function NuN_BuildMoneyString(moneyVal)
	local moneyTxt = nil;
	local gold, silver, copper;

	if ( ( moneyVal ) and ( moneyVal > 0 ) ) then
		if ( moneyVal > 9999 ) then
			gold = ( moneyVal / 10000 );
			gold = strformat("%d", gold);
			moneyVal = moneyVal - ( gold * 10000 );
		else
			gold = 0;
		end
		if ( moneyVal > 99 ) then
			silver = ( moneyVal / 100 );
			silver = strformat("%d", silver);
			moneyVal = moneyVal - ( silver * 100 );
		else
			silver = 0;
		end
		copper = moneyVal;
		moneyTxt = strformat("%dg %ds %dc", gold, silver, copper);
	end

	return moneyTxt;
end


function NuN_TextEscape(lFrame, lText)
	if ( NuNSettings[local_player.realmName].bHave ) then
		lText:ClearFocus();
		if ( ( lText:GetText() == nil ) or ( lText:GetText() == "" ) ) then
			lText:SetText("\n");
		end
	else
		lFrame:Hide();
	end
end



function NuN_OverTTCheckBox_OnClick()
	if ( NuN_OverTTCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].minOver = "1";
	else
		NuNSettings[local_player.realmName].minOver = nil;
	end
end

function NuN_ChatTagCheckBox_OnClick()
	if ( NuN_ChatTagCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].chatty = "1";
	else
		NuNSettings[local_player.realmName].chatty = nil;
	end
end


function NuN_AutoPartyCheckBox_OnClick()
	if ( NuN_AutoPartyCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoP = "1";
		NuN_ProcessParty();
	else
		NuNSettings[local_player.realmName].autoP = nil;

	end
end


function NuN_BehaveCheckBox_OnClick()
	if ( NuN_BehaveCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].bHave = "1";
	else
		NuNSettings[local_player.realmName].bHave = nil;
	end
end


function NuN_DeleteNote(dType)
	if ( NuNcDeleteFrame:IsVisible() ) then
		NuNcDeleteFrame:Hide();
	end
	NuNcDeleteFrame.type = dType;
	if ( dType == "Contact" ) then
		NuNcDeleteLabel:SetText(NUN_CONTACT_TXT.." :\n"..local_player.currentNote.unit);
		NuNcDeleteFrame:Show();
		NuNText:ClearFocus();
		NuNcDeleteGhostTextBox:SetFocus();
	else
		if ( NuNGNoteFrame.fromQuest ) then
			NuNcDeleteLabel:SetText(NuNC.NUN_QUEST_NOTE.." :\n"..local_player.currentNote.general);
		else
			NuNcDeleteLabel:SetText(NUN_GENERAL_TXT.." :\n"..local_player.currentNote.general);
		end
		NuNcDeleteFrame:Show();
		NuNGNoteTextScroll:ClearFocus();
		NuNcDeleteGhostTextBox:SetFocus();
	end
end


function NuNcDeleteButton_OnClick()
	if ( NuNcDeleteFrame.type == "Contact" ) then
		NuNcDeleteFrame:Hide();
		NuN_Delete();
	elseif ( NuNcDeleteFrame.type == "General" ) then
		NuNcDeleteFrame:Hide();
		NuNGNote_Delete();
	end
end


function NuN_LocStrip(locData)
	if ( locData ) then
		local p = strfind(locData, " : ");
		if ( p ) then
			locData = strsub(locData, (p+3));
		end
	end

	return locData;
end




function NuN_MapNote(MNType, MNxtra1, MNxtra2, MNColour)
	local MNCont, MNZone, x, y;
	local checknote = nil;
	local nKey = nil;

	if ( MetaMap_Quicknote ) then

	elseif ( MapNotes_OnLoad ) then
		MNCont, MNZone, x, y, checknote, nKey = NuN_GetMapNotesKey();
		if ( ( not MNCont ) or ( ( x == 0 ) and ( y == 0 ) ) ) then
			if ( MapNotes_OnLoad ) then
				MapNotes_StatusPrint(MAPNOTES_QUICKNOTE_NOPOSITION);
			end
	        	return;
		end
	else
		return;
	end

	local MNLine1, MNLine2, MNAuthor, NuN_Reaction;
	local MNName;
	local tName = UnitName("target");
	if ( ( MNType == "Target" ) and ( tName ) and ( not UnitPlayerControlled("target") ) ) then
		NuN_Reaction = UnitReaction("player", "target");
		if ( not MNColour ) then
			if ( NuN_Reaction < 4 ) then
				MNColour = 1;
			elseif ( NuN_Reaction == 4 ) then
				MNColour = 0;
			end
		end
		MNName = UnitName("target");
	else
		if ( not local_player.currentNote.general ) then
			if ( NuNGNoteTitleButton:IsVisible() ) then
				local_player.currentNote.general = NuNGNoteTitleButtonText:GetText();
			else
				local_player.currentNote.general = NuNGNoteTextBox:GetText();
			end
		end
		MNName = local_player.currentNote.general;
		if ( ( not MNName ) or ( MNName == "" ) ) then
			return;
		end
	end

	if ( not MNColour ) then
		MNColour = 3;
	end

	if ( MetaMap_Quicknote ) then
		MetaMap_Quicknote(MNName);
		return;
	end

	local theData;
	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		theData = MapNotes_Data_Notes[MNCont];					-- + v5.00.11200
	end

	if (checknote) then
		if ( ( ( NuN_State.inBG ) or ( MapNotes_Data_Notes ) ) and ( MNName == theData[checknote].name ) ) then		-- c v5.00.11200
			NuN_ReLinkMapNote(MNName, MNCont, MNZone, checknote);
			return;
		elseif ( not MapNotes_Data_Notes ) then
			if ( ( not NuN_State.inBG ) and ( MNName == theData[MNZone][checknote].name ) ) then
				NuN_ReLinkMapNote(MNName, MNCont, MNZone, checknote);
				return;
			end
		end
		local mergeFailed = NuN_MergeMapNote(MNCont, MNZone, checknote, MNName, nKey, MNxtra1, MNxtra2);
		if ( mergeFailed ) then
			local repName;
			if ( ( NuN_State.inBG ) or ( ( MapNotes_Data_Notes ) and ( MNZone == 0 ) ) ) then							-- c v5.00.11200
				repName = theData[checknote].name;
			else
				repName = theData[MNZone][checknote].name;
			end
			if ( MapNotes_OnLoad ) then
				MapNotes_StatusPrint( format(MAPNOTES_QUICKNOTE_NOTETONEAR, repName));
			end
			NuN_Message( NUN_MAX_MERGED );
		end
	else
		MNLine1 = MNxtra1;
		MNLine2 = MNxtra2;
		MNAuthor = "NotesUNeed - "..locals.player_Name;
		NuN_WriteMapNote(MNCont, MNZone, x, y, MNColour, MNName, MNLine1, MNLine2, MNAuthor);
	end
end

function NuN_ReLinkMapNote(MNName, MNCont, MNZone, tmpID)
	local NuN_Key = MNCont.."-"..MNZone.."-"..tmpID;
	if ( not NuNData[locals.mrgIndex_dbKey] ) then
		NuNData[locals.mrgIndex_dbKey] = {};
	end
	if ( not NuNData[locals.mrgIndex_dbKey][NuN_Key] ) then
		NuNData[locals.mrgIndex_dbKey][NuN_Key] = {};
		NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = 0;
	end
	if ( NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] ) then
--		NuN_Message(NuN_Strings.NUN_NOTESUNEED_INFO);
		return;
	end
	if ( not NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter ) then
		NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = 0;
	end
	NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter + 1;
	NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] = "1";
	if ( MapNotes_OnLoad ) then
		NuN_Message("NotesUNeed <> MapNote");
	end
end

function NuN_MergeMapNote(MNCont, MNZone, id, MNName, NuN_Key, MNxtra1, MNxtra2)
	local Merged = nil;
	local MNLine1, MNLine2, MNAuthor;
	local mrgEntry = nil;
	local oriNote;
	local mapNoted = true;
	local theData;

	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		theData = MapNotes_Data_Notes[MNCont];

	else
		return;
	end
	oriNote = theData[id].name;

	if ( not strfind(oriNote, MNName) ) then
		mapNoted = nil;
	end

	if ( not NuNData[locals.mrgIndex_dbKey] ) then
		NuNData[locals.mrgIndex_dbKey] = {};
	end

	if ( not NuNData[locals.mrgIndex_dbKey][NuN_Key] ) then
		NuNData[locals.mrgIndex_dbKey][NuN_Key] = {};

	else
		mrgEntry = true;
	end

	if ( mrgEntry ) then
		if ( not NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter ) then
			local counter = 0;
			local idx, value;
			for idx, value in pairs(NuNData[locals.mrgIndex_dbKey][NuN_Key]) do
				counter = counter + 1;
			end
			NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = counter;
		end
		if ( NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter > 4 ) then
			return "Failed";
		end
		if ( NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] ) then
			return nil;
		else
			NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter + 1;
			NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] = "1";
		end

	elseif ( mapNoted ) then
		NuN_ReLinkMapNote(MNName, MNCont, MNZone, id);

	else
		if ( ( not NuNDataANotes[oriNote] ) and ( not NuNDataRNotes[oriNote] ) ) then
			NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = 1;
			NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] = "1";
		else
			NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = 2;
			NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] = "1";
			NuNData[locals.mrgIndex_dbKey][NuN_Key][oriNote] = "1";
		end
	end

	if ( not mapNoted ) then
		theData[id].name = theData[id].name.." | "..MNName;
		theData[id].inf1 = theData[id].inf1.." | "..MNxtra1;
		theData[id].inf2 = theData[id].inf2.." | "..MNxtra2;
		theData[id].creator = NUN_POPUP_TITLE.." - "..locals.player_Name;
	end

	if ( MapNotes_OnLoad ) then
			MapNotes_StatusPrint( format(NUN_MERGING.." "..theData[id].name) );
	end

	return nil;
end


function NuN_WriteMapNote(MNCont, MNZone, x, y, MNColour, MNName, MNLine1, MNLine2, MNAuthor)
	local id = 0;
	local NuN_Key;
	local theData, theMiniData;
	local NuN_SetNextAsMiniNote;
	local numNotes, i, j, tmpID;

	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		theData = MapNotes_Data_Notes[MNCont];					-- + v5.00.11200
		theMiniData = MapNotes_MiniNote_Data;					-- + v5.00.11200
		NuN_SetNextAsMiniNote = MapNotes_SetNextAsMiniNote;			-- + v5.00.11200
		numNotes = MapNotes_NotesPerZone;					-- + v5.00.11200
		i = MapNotes_GetZoneTableSize(theData);					-- + v5.00.11200

	else
		return;
	end

	if (NuN_SetNextAsMiniNote ~= 2) then
		if ( ( not numNotes ) or ( ( numNotes ) and (i < numNotes) ) ) then
			tmpID = i + 1;
			theData[tmpID] = {};
			theData[tmpID].name = MNName;
			theData[tmpID].ncol = 0;
			theData[tmpID].inf1 = MNLine1;
			theData[tmpID].in1c = 0;
			theData[tmpID].inf2 = MNLine2;
			theData[tmpID].in2c = 0;
			theData[tmpID].creator = MNAuthor;
			theData[tmpID].icon = MNColour;
			theData[tmpID].xPos = x;
			theData[tmpID].yPos = y;

			if (NuN_SetNextAsMiniNote ~= 0) then
				theData[tmpID].mininote = true;
			end

			MapNotes_StatusPrint(format(MAPNOTES_QUICKNOTE_OK, GetRealZoneText()));

			NuN_Key = MNCont.."-"..MNZone.."-"..tmpID;
			if ( not NuNData[locals.mrgIndex_dbKey] ) then
				NuNData[locals.mrgIndex_dbKey] = {};
			end
			if ( not NuNData[locals.mrgIndex_dbKey][NuN_Key] ) then
				NuNData[locals.mrgIndex_dbKey][NuN_Key] = {};
			end
			NuNData[locals.mrgIndex_dbKey][NuN_Key].noteCounter = 1;
			NuNData[locals.mrgIndex_dbKey][NuN_Key][MNName] = "1";
		else
			if ( MapNotes_OnLoad ) then
				MapNotes_StatusPrint(format(MAPNOTES_QUICKNOTE_TOOMANY, GetRealZoneText()));
			end
		end
	end

end


function NuN_GetMapNotesKey()
	local id = nil;
	local nKey = nil;
	local x, y;
	local MNCont = nil;
	local MNZone = nil;

	NuN_State.inBG = false;

	SetMapToCurrentZone();

	x, y = GetPlayerMapPosition("player");
	if ( ( ( x == 0 ) and ( y == 0 ) ) or ( MNCont == 0 ) ) then
		return nil;
	end

	if ( MapNotes_Data_Notes ) then									-- + v5.00.11200
		MNCont = "WM ";										-- + v5.00.11200
		local map = GetMapInfo();								-- + v5.00.11200
		if ( map ) then										-- + v5.00.11200
			MNCont = MNCont..map;								-- + v5.00.11200
		else											-- + v5.00.11200
			MNCont = MNCont.."WorldMap";							-- + v5.00.11200
		end											-- + v5.00.11200
		MNZone = 0;										-- + v5.00.11200
		id = MapNotes_CheckNearNotes(MNCont, x, y);						-- + v5.00.11200
	end												-- + v5.00.11200

	if ( id ) then
		nKey = MNCont.."-"..MNZone.."-"..id;
	end

	return MNCont, MNZone, x, y, id, nKey;
end

-- NuN equivalent of the MapNotes proximity to other map notes check
function NuN_ProximityCheck(theBG, xPos, yPos)
	local chkData = {};
	local minDiff;

	local chkData = {};

	if ( MapNotes_OnLoad ) then
		chkData = MapNotes_Data[theBG];
		minDiff = MapNotes_MinDiff;
	end

	if ( ( not minDiff ) or ( minDiff == 0 ) ) then
		minDiff = 7;
	end

	if ( not chkData ) then
		return;
	end

	local i = 1;
	for j, value in pairs(chkData) do
		local deltax = abs(chkData[i].xPos - xPos);
		local deltay = abs(chkData[i].yPos - yPos);
		if(deltax <= 0.0009765625 * minDiff and deltay <= 0.0013020833 * minDiff) then
			return i;
		end
		i = i + 1;
	end

	return nil;
end


-- 5.60
function NuN_AutoMapCheckBox_OnClick()
	if ( NuN_AutoMapCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoMapNotes = "1";
	else
		NuNSettings[local_player.realmName].autoMapNotes = nil;
	end
end


-- 5.60
function NuN_GuildRefreshCheckBox_OnClick()
	if ( NuN_GuildRefreshCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoGuildNotes = "1";
		NuN_GRVerboseCheckBox:Enable();
		NuN_GRVerboseCheckBox:SetChecked(true);					-- Default verbose guild reporting when turning on
		NuNSettings[local_player.realmName].autoGRVerbose = "1";
		NuN_SyncGuildMemberNotes( NuNSettings[local_player.realmName].autoGRVerbose );	-- Trigger an immediate Guild Refresh
	else
		NuNSettings[local_player.realmName].autoGuildNotes = nil;
		NuNSettings[local_player.realmName].autoGRVerbose = nil;
		NuN_GRVerboseCheckBox:SetChecked(false);
		NuN_GRVerboseCheckBox:Disable();
	end
end


-- 5.60
function NuN_GRVerboseCheckBox_OnClick()
	if ( NuN_GRVerboseCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].autoGRVerbose = "1";
		NuN_SyncGuildMemberNotes( NuNSettings[local_player.realmName].autoGRVerbose );	-- Trigger an immediate Guild Refresh
	else
		NuNSettings[local_player.realmName].autoGRVerbose = nil;
	end
end

-- 5.60
function NuN_ModifierMasterCheckBox_OnClick()
	if ( NuN_ModifierMasterCheckBox:GetChecked() ) then
		NuNSettings[local_player.realmName].modifier = "on";
		NuNOptionsModifier:Enable();
		NuNOptions_SetModifierText();
	else
		NuNSettings[local_player.realmName].modifier = "off";
		NuNOptionsModifier:SetText("n/a");
		NuNOptionsModifier:Disable();
	end
end

-- Dump the Hyperlinks in the Notes main text to the chat frame, where the user can click on them
function NuN_HyperButton_OnClick(nType)
	local p1, p2;
	local linkA = {};
	local aCounter = 0;
	local lText, lTextLen;

	if ( nType == "Contact" ) then
		lText = NuNF.NuN_GetSelectedText(NuNText);
	elseif ( nType == "General" ) then
		lText = NuNF.NuN_GetSelectedText(NuNGNoteTextBox);
	end

	if ( not lText ) then
		if ( nType == "Contact" ) then
			lText = NuNText:GetText();
		elseif ( nType == "General" ) then
			lText = NuNGNoteTextScroll:GetText();
		else
			NuN_Message(NUN_LINKFAILURE);
			return;
		end
	end

	lTextLen = strlen(lText);

--NuN_Message("[DEBUG] NuN_HyperButton_OnClick - nType:" .. tostring(nType) .. "  text:" .. tostring(lText));

	p1 = strfind(lText, "|Hitem");
	while ( ( p1 ) and ( p1 > 10 ) ) do
		p2 = strfind(lText, "|h|r", p1);
		if ( ( not p2 ) or ( (p2+3) > lTextLen ) ) then
			break;
		end
		p1 = p1 - 10;
		p2 = p2 + 3;
		local link = strsub(lText, p1, p2);
		aCounter = aCounter + 1;
		linkA[aCounter] = link;
		p1 = strfind(lText, "|Hitem", (p2+1));
	end

	p1 = strfind(lText, "|Henchant");
	while ( p1 ) do
		p2 = strfind(lText, "|h|r", p1);
		if ( ( not p2 ) or ( (p2+3) > lTextLen ) ) then
			break;
		end
		p2 = p2 + 3;
		local link = strsub(lText, p1, p2);
		aCounter = aCounter + 1;
		linkA[aCounter] = link;
		p1 = strfind(lText, "|Henchant", (p2+1));
	end

	p1 = strfind(lText, "|Hspell");
	while ( p1 ) do
		p2 = strfind(lText, "|h|r", p1);
		if ( ( not p2 ) or ( (p2+3) > lTextLen ) ) then
			break;
		end
		p1 = p1 - 10;
		p2 = p2 + 3;
		local link = strsub(lText, p1, p2);
		aCounter = aCounter + 1;
		linkA[aCounter] = link;
		p1 = strfind(lText, "|Hspell", (p2+1));
	end

	local loops = getn(linkA);
	if ( loops ) and ( loops > 0 ) then
		if ( ChatFrame1EditBox and ChatFrame1EditBox:IsVisible() ) then
			local final_str, delim = "", ", ";
			for i=1, loops, 1 do
				if ( final_str ~= "" ) then
					final_str = final_str .. delim;
				end
				final_str = final_str .. linkA[i];
			end
			ChatFrame1EditBox:Insert(concat);
		else
			for i=1, loops, 1 do
				DEFAULT_CHAT_FRAME:AddMessage(linkA[i]);
			end
		end

	else
		NuN_Message(NUN_NOLINKS);
	end
end


-- MAPNOTES BEGIN

-- What to do when you click on the MapNotes index of NuN Notes
function NuNPopup_OnClick(bttn, noteN)
	if ( bttn == "RightButton" ) then
		if ( IsAltKeyDown() ) then
			NuN_DeleteMapIndexNote(NuNPopup.id, noteN);
		end
	elseif ( bttn == "LeftButton" ) then
		WorldMapFrame:Hide();
		NuNPopup:Hide();
		if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
			return;
		end
		if ( NuNGNoteFrame:IsVisible() ) then
			if ( local_player.currentNote.general == noteN ) then
				return;
			end
			NuNGNoteFrame:Hide();
		end
		NuNGNoteFrame.fromQuest = nil;
		local_player.currentNote.general = noteN;
		NuN_ShowSavedGNote();
	end
end

function NuNPopup_OnShow()
	locals.popUpHide = nil;
end

function NuNPopup_OnUpdate(arg1)
	locals.popUpTimeSinceLastUpdate = locals.popUpTimeSinceLastUpdate + arg1;
	if ( locals.popUpTimeSinceLastUpdate > 0.1 ) then
		if ( ( locals.popUpHide ) and ( not IsControlKeyDown() ) ) then
			NuNPopup:Hide();
			locals.popUpHide = nil;
		end
--		if ( ( locals.popUpHide ) and ( not MouseIsOver(NuNPopup) ) and ( not IsControlKeyDown() ) ) then
--			NuNPopup:Hide();
--			if ( NuNHooks.NuNOri_MapNotes_OnLeave ) then
--				NuNHooks.NuNOri_MapNotes_OnLeave(NuNPopup.id);
--			end
--		end
		locals.popUpTimeSinceLastUpdate = 0;
	end
end

function NuN_UpdateMapNotesIndex(deletedNote)
	local MNindex, MNvalue, nIndex, nValue;

	for MNindex, MNvalue in pairs(NuNData[locals.mrgIndex_dbKey]) do
		for nIndex, nValue in pairs(NuNData[locals.mrgIndex_dbKey][MNindex]) do
			if ( nIndex == deletedNote ) then
				NuNData[locals.mrgIndex_dbKey][MNindex][nIndex] = nil;
				NuNData[locals.mrgIndex_dbKey][MNindex].noteCounter = NuNData[locals.mrgIndex_dbKey][MNindex].noteCounter - 1;
				break;
			end
			if ( NuNData[locals.mrgIndex_dbKey][MNindex].noteCounter < 1 ) then
				NuNData[locals.mrgIndex_dbKey][MNindex] = nil;
			end
		end
	end
end



function NuN_PreDeleteMapIndex(id, cont, zone)
	local curZ, lstEntry;

	if ( MapNotes_Data_Notes ) then								-- + v5.00.11200
		if ( not cont ) then
			cont = GetMapInfo();
			if ( cont ) then
				cont = "WM "..cont;
			else
				cont = "WM WorldMap";
			end
		end
		if ( MapNotes_Data_Notes[cont] ) then
			curZ = MapNotes_Data_Notes[cont];						-- + v5.00.11200
		end
	end											-- + v5.00.11200

	lstEntry = MapNotes_GetZoneTableSize(curZ);
	return cont, zone, lstEntry;
end

-- Called when a MapNote is deleted

function NuN_DeleteMapIndex(id, cont, zone, lstEntry)
	if ( not zone ) then									-- + v5.00.11200
		zone = 0;									-- + v5.00.11200
	end											-- + v5.00.11200
	local nKey = cont.."-"..zone.."-"..id;
	local lstKey = cont.."-"..zone.."-"..lstEntry;
	if ( NuNData[locals.mrgIndex_dbKey][nKey] ) then
		NuNData[locals.mrgIndex_dbKey][nKey] = nil;
	end
	if ( ( lstEntry ~= 0 ) and ( nKey ~= lstKey ) and ( NuNData[locals.mrgIndex_dbKey][lstKey] ) ) then
		local idx, value;
		NuNData[locals.mrgIndex_dbKey][nKey] = {};
		for idx, value in pairs(NuNData[locals.mrgIndex_dbKey][lstKey]) do
			NuNData[locals.mrgIndex_dbKey][nKey][idx] = NuNData[locals.mrgIndex_dbKey][lstKey][idx];
		end
		NuNData[locals.mrgIndex_dbKey][lstKey] = nil;
	end
end



-- Called when Alt-Right Clicking on a button in the Popup to remove a single NuN - MapNote link

function NuN_DeleteMapIndexNote(id, noteN)
	local cont, zone;									-- + v5.00.11200

	if ( MapNotes_Data_Notes ) then								-- + v5.00.11200
		cont = "WM ";									-- + v5.00.11200
		local map = GetMapInfo();							-- + v5.00.11200
		if ( map ) then									-- + v5.00.11200
			cont = cont..map;							-- + v5.00.11200
		else										-- + v5.00.11200
			cont = cont.."WorldMap";						-- + v5.00.11200
		end										-- + v5.00.11200
		zone = 0;									-- + v5.00.11200
	end											-- + v5.00.11200

	local nKey = cont.."-"..zone.."-"..id;
	if ( ( NuNData[locals.mrgIndex_dbKey][nKey] ) and ( NuNData[locals.mrgIndex_dbKey][nKey][noteN] ) ) then
		NuNData[locals.mrgIndex_dbKey][nKey][noteN] = nil;
		NuNData[locals.mrgIndex_dbKey][nKey].noteCounter = NuNData[locals.mrgIndex_dbKey][nKey].noteCounter - 1;
		if ( NuNData[locals.mrgIndex_dbKey][nKey].noteCounter < 1 ) then
			NuNData[locals.mrgIndex_dbKey][nKey] = nil;
		end
	end
	WorldMapTooltip:Hide();
	NuNPopup:Hide();
	NuN_MapTooltip:Hide();
end



function NuN_MapIndexHouseKeeping()
	local MNindex, MNvalue;

	if ( ( MetaMap_Quicknote ) and ( MapNotes_OnLoad ) ) then
		return;
	end

	for MNindex, MNvalue in pairs(NuNData[locals.mrgIndex_dbKey]) do
		local valid = NuN_ExtractMapNotesInfo(MNindex);
		if ( not valid ) then
			NuNData[locals.mrgIndex_dbKey][MNindex] = nil;
		end
	end
end


function NuN_ExtractMapNotesInfo(nKey)
	local cont, zone, id;
	local sep = "-";
	local p, q;

	p = strfind(nKey, sep);
	if ( not p ) then
		return nil;
	end
	q = strfind(nKey, sep, (p+1));
	if ( not q ) then
		return nil;
	end

	cont = tonumber( strsub(nKey, 1, (p-1)) );
	zone = tonumber( strsub(nKey, (p+1), (q-1)) );
	id = tonumber( strsub(nKey, (q+1)) );

	if ( MapNotes_Data_Notes ) then							-- + v5.00.11200
		theData = MapNotes_Data_Notes;						-- + v5.00.11200
	end

	if ( ( zone == 0 ) and ( theData[cont] ) and ( theData[cont][id] ) ) then
		return true;

	elseif ( ( theData[cont] ) and ( theData[cont][zone] ) and ( theData[cont][zone][id] ) ) then
		return true;

	else
		return nil;
	end
end

-- MAPNOTES END
function NuN_LoadLastOpenedNote()
	local loadedSuccessfully = false;
	if NuNSettings and local_player.realmName and NuNSettings[local_player.realmName] and NuNSettings[local_player.realmName].lastNote then
		loadedSuccessfully = true;
		if NuNSettings[local_player.realmName].lastNote.type == "Contact" then
			locals.NuN_LastOpen.type = "Contact";
			locals.NuN_LastOpen.name = NuNSettings[local_player.realmName].lastNote.name;
		elseif NuNSettings[local_player.realmName].lastNote.type == "General" then
			locals.NuN_LastOpen.type = "General";
			locals.NuN_LastOpen.note = NuNSettings[local_player.realmName].lastNote.name;
		else
			NuN_Message("Invalid value loaded for the last-opened-note's type:" .. tostring(NuNSettings[local_player.realmName].lastNote.type) .. " (" .. type(NuNSettings[local_player.realmName].lastNote.type));
		end
	end
end
function NuN_SaveLast(saveType)
	locals.NuN_LastOpen.type = saveType;
	if ( saveType == "Contact" ) then
		locals.NuN_LastOpen.name = local_player.currentNote.unit;

		-- save the persistent storage
		NuNSettings[local_player.realmName].lastNote = {};
		NuNSettings[local_player.realmName].lastNote.type = "Contact";
		NuNSettings[local_player.realmName].lastNote.name = locals.NuN_LastOpen.name;
	elseif ( saveType == "General" ) then
		locals.NuN_LastOpen.note = local_player.currentNote.general;

		-- save the persistent storage
		NuNSettings[local_player.realmName].lastNote = {};
		NuNSettings[local_player.realmName].lastNote.type = "General";
		NuNSettings[local_player.realmName].lastNote.name = locals.NuN_LastOpen.note;
	end
end


function NuN_ReOpen()
	if ( locals.NuN_LastOpen.type ) then
		if ( locals.NuN_LastOpen.type == "Contact" ) then
			if ( locals.NuNDataPlayers[locals.NuN_LastOpen.name] ) then
				NuN_ShowSavedNote(locals.NuN_LastOpen.name);
			end
		elseif ( locals.NuN_LastOpen.type == "General" ) then
			if ( ( receiptPending ) and ( locals.NuN_Receiving.type == "General" ) ) then
				return;
			end
			if ( ( NuNDataRNotes[locals.NuN_LastOpen.note] ) or ( NuNDataANotes[locals.NuN_LastOpen.note] ) ) then
				local_player.currentNote.general = locals.NuN_LastOpen.note;
				NuN_ShowSavedGNote();
			end
		end
	end
end



function NuN_RunButton_OnClick()
	local script = NuNGNoteTextScroll:GetText();

	RunScript(script);
end



NuN_GetTipAnchor = function(theTT)
	local anchorBy, anchorTo;
	local x1, y1 = theTT:GetCenter();
	local x2, y2 = UIParent:GetCenter();

	if ( theTT == ShoppingTooltip1 ) then
		anchorBy = "TOPLEFT";
		anchorTo = "TOPRIGHT";
		return anchorBy, anchorTo;
	elseif ( theTT == ShoppingTooltip2 ) then
		anchorBy = "TOPLEFT";
		anchorTo = "BOTTOMLEFT";
		return anchorBy, anchorTo;
	end

	if ( ( not x1 ) or ( not y1 ) ) then
		anchorBy = "BOTTOMRIGHT";
		anchorTo = "BOTTOMLEFT";
	else
		if ( y1 > y2 ) then
			if ( x1 > x2 ) then
				anchorBy = "TOPRIGHT";
				anchorTo = "TOPLEFT";
			else
				anchorBy = "TOPLEFT";
				anchorTo = "TOPRIGHT";
			end
		else
			if ( x1 > x2 ) then
				anchorBy = "BOTTOMRIGHT";
				anchorTo = "BOTTOMLEFT";
			else
				anchorBy = "BOTTOMLEFT";
				anchorTo = "BOTTOMRIGHT";
			end
		end
	end

	return anchorBy, anchorTo;
end


--------------------------------------------------------------------------
-- Create the note when receiving data from another Player --

function NuN_CreateReceivedNote()
	if ( ( locals.NuN_Receiving.active ) and ( not receiptPending ) ) then
		if ( not locals.NuN_Receiving.version ) then
			return;
		end
		if ( ( not locals.NuN_Receiving.title ) or ( locals.NuN_Receiving.title == "" ) ) then
			return;
		end
		NuN_WriteReceiptLog();
		if ( locals.NuN_Receiving.type == "General" ) then
			if ( NuNGNoteFrame:IsVisible() ) then
				if ( not NuNGNoteFrame.fromQuest ) then
					NuNGNote_WriteNote();
					NuN_Message(NUN_SAVED_NOTIFY1.."\""..local_player.currentNote.general.."\""..NUN_SAVED_NOTIFY2);
					NuN_Message(NUN_SAVED_NOTIFY3.."\""..NuN_Receiving.title.."\"");
				end
				NuNGNoteFrame:Hide();
			end
			local_player.currentNote.general = locals.NuN_Receiving.title;
			contact.type = locals.NuN_Receiving.subtype;
			general.text = "";
			local nL = "";
			for idx, value in ipairs(locals.NuN_Receiving.text) do
				general.text = general.text..nL..NuN_Receiving.text[idx];
				nL = "\n";
			end
			NuN_ShowTitledGNote(general.text);
			NuNGNote_WriteNote();

		elseif ( locals.NuN_Receiving.type == "Contact" ) then
			if ( NuNFrame:IsVisible() ) then
				NuN_WriteNote();
				HideNUNFrame();
				NuN_Message(NUN_SAVED_NOTIFY1.."\""..local_player.currentNote.unit.."\""..NUN_SAVED_NOTIFY2);
				NuN_Message(NUN_SAVED_NOTIFY3.."\""..NuN_Receiving.title.."\"");
			end
			NuN_ShowReceivedContact();
			if ( locals.NuNDataPlayers[local_player.currentNote.unit] ) then
				NuN_SearchForNote("Text", local_player.currentNote.unit);
				receiptPending = true;
				StaticPopup_Show("NUN_DUPLICATE_RECORD");
			else
				NuN_WriteNote();
				NuNHeader:SetText(NUN_SENT.." : "..local_player.currentNote.unit);
			end
		end

	end
end


function NuN_WriteReceiptLog()
	local saveLvl;
	local tmp_c_note = local_player.currentNote.general;
	local tmp_g_text = general.text;
	locals.rMsgSeq = locals.rMsgSeq + 1;
	if ( locals.rMsgSeq > 9 ) then
		locals.rMsgSeq = 1;
	end
	local_player.currentNote.general = "Receipt Log "..locals.player_Name..rMsgSeq..":";
	if ( not locals.NuN_Receiving.title ) then
		locals.NuN_Receiving.title = "T?";
	end
	if ( not locals.NuN_Receiving.prefix ) then
		locals.NuN_Receiving.prefix = "P?";
	end
	if ( not locals.NuN_Receiving.log ) then
		locals.NuN_Receiving.log = "L?";
	end
	general.text = locals.NuN_Receiving.title.."\n"..NuN_Receiving.prefix.." : "..NuNF.NuN_GetDateStamp().."\n\n"..NuN_Receiving.log;

	if ( NuNSettings[local_player.realmName].dLevel ) then
		NuNDataANotes[local_player.currentNote.general] = {};
		NuNDataANotes[local_player.currentNote.general].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		saveLvl = "Account";
	else
		NuNDataRNotes[local_player.currentNote.general] = {};
		NuNDataRNotes[local_player.currentNote.general].type = NuNGet_CommandID(NUN_NOTETYPES, "LOG");
		saveLvl = "Realm";
	end
	NuNF.NuN_SetGText(saveLvl);
	local_player.currentNote.general = tmp_c_note;
	general.text = tmp_g_text;
end


function NuN_ShowReceivedContact()
	local_player.currentNote.unit = locals.NuN_Receiving.title;
	contact.route = "Receipt";

	if ( locals.NuN_Receiving.faction == "Horde" ) then
		c_faction = "Horde";
		NuNF.NuN_HordeSetup();
	else
		c_faction = "Alliance";
		NuNF.NuN_AllianceSetup();
	end


	-- Get note Text
	contact.text = "";
	if ( locals.NuN_Receiving.text ) then
		local nL = "";
		for idx, value in ipairs(locals.NuN_Receiving.text) do
			contact.text = contact.text..nL..NuN_Receiving.text[idx];
			nL = "\n";
		end
		local lenCheck = strlen(contact.text);
		if ( lenCheck > NuNC.NUN_MAX_TXT_LIM ) then
			NuN_Message(NUN_RECEIPT_TRUNCATION_WARNING);
			contact.text = strsub(contact.text, 1, NuNC.NUN_MAX_TXT_LIM);
		end
	end

	locals.prevName = local_player.currentNote.unit;

	if ( NuNOptionsFrame:IsVisible() ) then
		NuNOptionsFrame:Hide();
	end

	locals.lastDD = nil;
	NuNButtonClrDD:Disable();

	NuNF.ClearButtonChanges();

	NuNHeader:SetText(NUN_SENT.." : "..local_player.currentNote.unit);
	NuNText:SetText( contact.text );
	NuNButtonDelete:Disable();
	NuNCOpenChatButton:Disable();
	NuNCTTCheckBoxLabel:Hide();
	NuN_CTTCheckBox:Hide();

	contact.race = nil;
	contact.class = nil;
	contact.sex = nil;
	contact.prof1 = nil;
	contact.prof2 = nil;
	contact.arena = nil;
	contact.hrank = nil;

	-- Process Drop Down Boxes if Sender has v3.50 or later
	if ( locals.NuN_Receiving.version ) then
		-- If Sender has different client, create a dummy note for language translation processing
		if ( ( locals.NuN_Receiving.lang ~= NUN_CLIENT ) and ( ( locals.NuN_Receiving.lang == "German" ) or ( NUN_CLIENT == "German" ) ) ) then
			if ( locals.NuN_Receiving.lang == "German" ) then
				langDir = "->en";
			else
				langDir = "->de";
			end
			locals.NuNDataPlayers[rBuff] = {};
			locals.NuNDataPlayers[rBuff].faction = locals.NuN_Receiving.faction;
			locals.NuNDataPlayers[rBuff].race = locals.NuN_Receiving.race;
			locals.NuNDataPlayers[rBuff].cls = locals.NuN_Receiving.class;
			locals.NuNDataPlayers[rBuff].prof1 = locals.NuN_Receiving.prof1;
			locals.NuNDataPlayers[rBuff].prof2 = locals.NuN_Receiving.prof2;
			NuN_LangPatch(langDir, rBuff);
			if ( locals.NuN_Receiving.race ) then
				locals.NuN_Receiving.race = locals.NuNDataPlayers[rBuff].race;
			end
			if ( locals.NuN_Receiving.class ) then
				locals.NuN_Receiving.class = locals.NuNDataPlayers[rBuff].cls;
			end
			if ( locals.NuN_Receiving.prof1 ) then
				locals.NuN_Receiving.prof1 = locals.NuNDataPlayers[rBuff].prof1;
			end
			if ( locals.NuN_Receiving.prof2 ) then
				locals.NuN_Receiving.prof2 = locals.NuNDataPlayers[rBuff].prof2;
			end
			locals.NuNDataPlayers[rBuff] = nil;
		end
		if ( locals.NuN_Receiving.race ~= nil ) then
			contact.race = locals.Races[locals.NuN_Receiving.race];
			locals.dropdownFrames.ddRace = locals.NuN_Receiving.race;
			UIDropDownMenu_SetSelectedID(locals.NuNRaceDropDown, locals.dropdownFrames.ddRace);
			UIDropDownMenu_SetText(locals.NuNRaceDropDown, contact.race);
		else
			locals.dropdownFrames.ddRace = -1;
			UIDropDownMenu_ClearAll(locals.NuNRaceDropDown);
		end
		if ( locals.NuN_Receiving.class ~= nil ) then
			contact.class = locals.Classes[locals.NuN_Receiving.class];
			locals.dropdownFrames.ddClass = locals.NuN_Receiving.class;
			UIDropDownMenu_SetSelectedID(locals.NuNClassDropDown, locals.dropdownFrames.ddClass);
			UIDropDownMenu_SetText(locals.NuNClassDropDown, contact.class);
		else
			locals.dropdownFrames.ddClass = -1;
			UIDropDownMenu_ClearAll(locals.NuNClassDropDown);
		end
		if ( locals.NuN_Receiving.sex ~= nil ) then
			contact.sex = NUN_SEXES[locals.NuN_Receiving.sex];
			locals.dropdownFrames.ddSex = locals.NuN_Receiving.sex;
			UIDropDownMenu_SetSelectedID(NuNSexDropDown, locals.dropdownFrames.ddSex);
			UIDropDownMenu_SetText(NuNSexDropDown, contact.sex);
		else
			locals.dropdownFrames.ddSex = -1;
			UIDropDownMenu_ClearAll(NuNSexDropDown);
		end
		if ( locals.NuN_Receiving.prating ~= nil ) then
			locals.dropdownFrames.ddPRating = locals.NuN_Receiving.prating;
			contact.prating = NuNSettings.ratings[locals.NuN_Receiving.prating];
			UIDropDownMenu_SetSelectedID(NuNPRatingDropDown, locals.dropdownFrames.ddPRating);
			UIDropDownMenu_SetText(NuNPRatingDropDown, contact.prating);
		else
			locals.dropdownFrames.ddPRating = -1;
			UIDropDownMenu_ClearAll(NuNPRatingDropDown);
		end
		if ( locals.NuN_Receiving.prof1 ~= nil ) then
			locals.dropdownFrames.ddProf1 = locals.NuN_Receiving.prof1;
			contact.prof1 = NUN_PROFESSIONS[locals.NuN_Receiving.prof1];
			UIDropDownMenu_SetSelectedID(NuNProf1DropDown, locals.dropdownFrames.ddProf1);
			UIDropDownMenu_SetText(NuNProf1DropDown, contact.prof1);
		else
			locals.dropdownFrames.ddProf1 = -1;
			UIDropDownMenu_ClearAll(NuNProf1DropDown);
		end
		if ( locals.NuN_Receiving.prof2 ~= nil ) then
			locals.dropdownFrames.ddProf2 = locals.NuN_Receiving.prof2;
			contact.prof2 = NUN_PROFESSIONS[locals.NuN_Receiving.prof2];
			UIDropDownMenu_SetSelectedID(NuNProf2DropDown, locals.dropdownFrames.ddProf2);
			UIDropDownMenu_SetText(NuNProf2DropDown, contact.prof2);
		else
			locals.dropdownFrames.ddProf2 = -1;
			UIDropDownMenu_ClearAll(NuNProf2DropDown);
		end
		if ( locals.NuN_Receiving.arena ~= nil ) then
			locals.dropdownFrames.ddArena = locals.NuN_Receiving.arena;
			contact.arena = NUN_ARENAR[locals.NuN_Receiving.arena];
			UIDropDownMenu_SetSelectedID(NuNArenaRDropDown, locals.dropdownFrames.ddArena);
			UIDropDownMenu_SetText(NuNArenaRDropDown, contact.arena);
		else
			locals.dropdownFrames.ddArena = -1;
			UIDropDownMenu_ClearAll(NuNArenaRDropDown);
		end
		if ( locals.NuN_Receiving.hrank ~= nil ) then
			locals.dropdownFrames.ddHRank = locals.NuN_Receiving.hrank;
			contact.hrank = locals.Ranks[locals.NuN_Receiving.hrank];
			UIDropDownMenu_SetSelectedID(locals.NuNHRankDropDown, locals.dropdownFrames.ddHRank);
			UIDropDownMenu_SetText(locals.NuNHRankDropDown, contact.hrank);
		else
			locals.dropdownFrames.ddHRank = -1;
			UIDropDownMenu_ClearAll(locals.NuNHRankDropDown);
		end
	end



	-- Process the User Buttons, and populate pre-save array
	contact.guild = nil;
	gRank = nil;
	gRankIndex = nil;
	gNote = nil;
	gOfficerNote = nil;

	-- Have a step to check for sends from different clients
	--   If sent from a different language client, then need to check for default headings against the different languages defaults...
	--   If a different language default, then can substitute with this Clients default...
	if ( ( locals.NuN_Receiving.user ) and ( locals.NuN_Receiving.lang ~= NUN_CLIENT ) ) then
		if ( locals.NuN_Receiving.lang == "English" ) then
			NuN_xlateHeadings(locals.enHeadings);
		elseif ( locals.NuN_Receiving.lang == "German" ) then
			NuN_xlateHeadings(deHeadings);
		elseif ( locals.NuN_Receiving.lang == "French" ) then
			NuN_xlateHeadings(locals.frHeadings);
		end
	end

	-- Loop through the User buttons
	NuN_SetReceivedUserButtons();

	if ( NuNEditDetailsFrame:IsVisible() ) then
		NuNEditDetailsFrame:Hide();
	end
	if ( NuNcDeleteFrame:IsVisible() ) then
		NuNcDeleteFrame:Hide();
	end

--	NuNPartiedLabel:Hide();
	NuNPartiedNumberLabel:SetText("(0)");
	NuNPartiedNumberLabel:Hide();
	NuNFramePartyDownButton:Hide();

	NuNFrame:SetScale(NuNSettings[local_player.realmName].pScale);
	NuNFrame:Show();
	if ( not NuNSettings[local_player.realmName].bHave ) then
		NuNText:SetFocus();
	else
		NuNText:ClearFocus();
	end
end

function NuN_xlateHeadings()
	for i=1, locals.uBttns, 1 do
		if ( ( locals.NuN_Receiving.user[i] ) and ( locals.NuN_Receiving.user[i].title ) ) then
			if ( locals.NuN_Receiving.user[i].title == locals.fromHeadings[i] ) then
				locals.NuN_Receiving.user[i].title = NUN_DFLTHEADINGS[i];
			end
		end
	end
end

function NuN_SetReceivedUserButtons()
	for i=1, locals.uBttns, 1 do
		local bttnHeadingText = _G["NuNTitleButton"..i.."ButtonTextHeading"];
		local bttnDetailText = _G["NuNInforButton"..i.."ButtonTextDetail"];
		local bttnDetail = _G["NuNInforButton"..i];
local headingNumber = locals.pHead..i;
local headingName = local_player.currentNote.unit..locals.headingNumber;
local headingDate = local_player.currentNote.unit.. locals.pDetl .. i;
		bttnHeadingText:SetText(NUN_DFLTHEADINGS[i]);
		bttnDetailText:SetText("");
		bttnDetail:Enable();
		if ( locals.NuN_Receiving.user ) then
			if ( ( locals.NuN_Receiving.user[i] ) and ( locals.NuN_Receiving.user[i].title ) ) then
				-- if Receiver NOT using Global defaults <OR> we have not been sent a global default, then create Notes specific heading
				if ( ( NuNSettings[local_player.realmName][locals.headingNumber] ) or ( locals.NuN_Receiving.user[i].title ~= NUN_DFLTHEADINGS[i] ) ) then
					bttnHeadingText:SetText( locals.NuN_Receiving.user[i].title );
					locals.bttnChanges[i] = locals.NuN_Receiving.user[i].title;
				end
				-- process detail, only if sent a heading
				local hdngTest = bttnHeadingText:GetText();
				if ( ( not hdngTest ) or ( hdngTest == "" ) ) then
					bttnDetailText:SetText("");
					bttnDetail:Disable();
					locals.bttnChanges[i] = -1;					-- 5.60 Use -1 to flag blank
					locals.bttnChanges[i+locals.detlOffset] = -1;			-- 5.60 Use -1 to flag blank
				else
					if ( locals.NuN_Receiving.user[i].detl ) then
						bttnDetailText:SetText( locals.NuN_Receiving.user[i].detl );
						locals.bttnChanges[i+locals.detlOffset] = locals.NuN_Receiving.user[i].detl;
					end
				end
			end
		end
	end
end



function NuN_HidePopup(fType)
	if ( ( StaticPopup1 ) and ( StaticPopup1:IsVisible() ) ) then
		local testText = StaticPopup1Text:GetText();
		if ( testText == NUN_DUPLICATE ) then
			if ( fType == locals.NuN_Receiving.type ) then
				StaticPopup1:Hide();
			end
		end
	end
end

-- END of Receiving a NuN Note from another User Processing--
------------------------------------------------------------------------------

function NuN_SearchForNote(sType, sTitle)
	if ( ( type(sTitle) == "string" ) and ( strfind(sTitle, "|Hitem:") ) ) then
		local simpleTitle = NuNF.NuN_GetSimpleName(sTitle);
		if ( simpleTitle ~= nil ) then
			sTitle = simpleTitle;
		end
	end
	locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, sType);
	locals.searchType = NUN_SEARCHFOR[locals.dropdownFrames.ddSearch].Command;
	if ( sType == "Text" ) then
		NuNSearchTextBox:SetText(sTitle);
	end
	NuNOptions_Search();
end


-- Generic function for parsing the NotesUNeed database and executing fixes on the data
function NuN_DataFix1(NuN_FixFunction, fixParm1)
	local lKey = local_player.realmName;
	local debugCounter = 0;
	local questHistoryArray = {};

	suppressDateUpdate = true;

	for idx, value in pairs(NuNData) do
		if ( idx == locals.Notes_dbKey ) then
			for index2, value2 in pairs(NuNData[idx]) do
				if ( NuNData[idx][index2].txt ) then
					general.text = NuNF.NuN_GetGText(index2);
					if ( general.text ) then
						if ( ( NuN_FixFunction ) and ( type(NuN_FixFunction) == "function" ) ) then
							general.text = NuN_FixFunction(index2, general.text, fixParm1);
						end
						debugCounter = debugCounter + 1;
						local_player.currentNote.general = index2;
						NuNF.NuN_SetGText("Account");
					end
				end
			end
		elseif ( not strfind(idx, "~") ) then
			for index2, value2 in pairs(NuNData[idx]) do
				if ( NuNData[idx][index2].faction ) then
					local_player.realmName = idx;
					contact.text = NuNF.NuN_GetCText(index2);
					if ( contact.text ) then
						if ( ( NuN_FixFunction ) and ( type(NuN_FixFunction) == "function" ) ) then
							contact.text = NuN_FixFunction(index2, contact.text, fixParm1);
						end
						debugCounter = debugCounter + 1;
						NuNF.NuN_SetCText(index2);
					end
				elseif ( index2 == locals.Notes_dbKey ) then
					local_player.realmName = idx;
					for index3, value3 in pairs(NuNData[idx][index2]) do
						if ( NuNData[idx][index2][index3].txt ) then
							general.text = NuNF.NuN_GetGText(index3);
							if ( general.text ) then
								if ( ( NuN_FixFunction ) and ( type(NuN_FixFunction) == "function" ) ) then
									general.text = NuN_FixFunction(index3, general.text, fixParm1);
								end
								debugCounter = debugCounter + 1;
								local_player.currentNote.general = index3;
								NuNF.NuN_SetGText("Realm");
							end
						end
					end
				end
				if ( ( NuNData[idx].QuestHistory ) and ( not questHistoryArray[idx] ) ) then
					questHistoryArray[idx] = true;
					for player, players in pairs(NuNData[idx].QuestHistory) do
						for quest, quests in pairs(NuNData[idx].QuestHistory[player]) do
							if ( NuNData[idx].QuestHistory[player][quest].txt ) then
								local txt = NuNF.NuN_GetDisplayText( NuNData[idx].QuestHistory[player][quest].txt );
									if ( ( NuN_FixFunction ) and ( type(NuN_FixFunction) == "function" ) ) then
										txt = NuN_FixFunction(nil, txt, fixParm1);
									end
								txt = NuNF.NuN_SetSaveText(txt);
								NuNData[idx].QuestHistory[player][quest].txt = txt;
								debugCounter = debugCounter + 1;
							end
							if ( NuNData[idx].QuestHistory[player][quest].complete ) then
								local complete = NuNF.NuN_GetDisplayText( NuNData[idx].QuestHistory[player][quest].complete );
									if ( ( NuN_FixFunction ) and ( type(NuN_FixFunction) == "function" ) ) then
										complete = NuN_FixFunction(nil, complete, fixParm1);
									end
								complete = NuNF.NuN_SetSaveText(complete);
								NuNData[idx].QuestHistory[player][quest].complete = complete;
								debugCounter = debugCounter + 1;
							end
						end
					end
				end
			end
		end
	end
	local_player.realmName = lKey;
	suppressDateUpdate = nil;
end



-- Execute the contents of a note as a Lua Script
function NuN_ExecuteNote(parm1)
	local script;
	if ( NuNDataANotes[parm1] ) then
		script = NuNF.NuN_GetGText(parm1);
	elseif ( NuNDataRNotes[parm1] ) then
		script = NuNF.NuN_GetGText(parm1);
	else
		NuN_SearchForNote("Text", parm1);
		return nil;
	end

	RunScript(script);
	return true;
end



function NuN_Tooltip_OnHide()
	if ( not NuNSettings[local_player.realmName].tScale ) then
		NuNSettings[local_player.realmName].tScale = 1.0;
	end
	NuN_Tooltip:SetScale( NuNSettings[local_player.realmName].tScale );
end



function NuN_SearchFrameSearch(buttonName)
	if ( NuNSearchTextBox:GetText() ~= "" ) then
		locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "Text");
	else
		locals.dropdownFrames.ddSearch = NuNGet_CommandID(NUN_SEARCHFOR, "All");
	end
	NuNSearch_Search(buttonName);
end

function NuN_IndexAll()
	local curZ;

	if ( MapNotes_Data_Notes ) then						-- + v5.00.11200
		curZ = MapNotes_Data_Notes;					-- + v5.00.11200

	else
		return;
	end

	for cont, dataSet in pairs(curZ) do
		if ( type(cont) == "string" ) then
			NuN_IndexByZone(cont, 0, curZ[cont]);

		elseif ( type(cont) == "number" ) then
			for zone=0, 40, 1 do
				if ( curZ[cont][zone] ) then
					NuN_IndexByZone(cont, zone, curZ[cont][zone]);
				end
			end
		end
	end
end

function NuN_IndexByZone(cont, zone, curZ)
	local nName, preKey, nKey = "", (cont.."-"..zone.."-"), "";

	for idx, dataSet in pairs(curZ) do
		nName = dataSet.name;
		if ( nName ) then
			nKey = preKey .. idx;
			if ( NuNData[locals.itmIndex_dbKey][nName] ) then
				nName = ( NuNData[locals.itmIndex_dbKey][nName] );
			end
			if ( ( NuNDataRNotes[nName] ) or ( NuNDataANotes[nName] ) ) then
				NuN_CreateIndex(nKey, nName);
			else
				local c = " | ";
				NuN_SubStrings(nKey, nName, c);
				local c = " \124\124 ";
				NuN_SubStrings(nKey, nName, c);
			end
		end
	end
end

-- This function splits up MapNote names which have been generated by Merging multiple NotesUNeed Notes
-- e.g.  "Note 1 | Note 2 | Note 3"  where 3 notes are linked to the MapNote
function NuN_SubStrings(nKey, nName, sep)
	local p1, p2, sName = 1, -1, "";
	local len = strlen(sep);

	p2 = strfind(nName, sep);
	while ( p2 ) do
		sName = strsub(nName, p1, p2-1);
		if ( NuNData[locals.itmIndex_dbKey][sName] ) then
			sName = ( NuNData[locals.itmIndex_dbKey][sName] );
		end
		if ( ( sName ) and ( ( NuNDataRNotes[sName] ) or ( NuNDataANotes[sName] ) ) ) then
			NuN_CreateIndex(nKey, sName);
		end
		p1 = p2+len;
		p2 = strfind(nName, sep, p1);
		if ( not p2 ) then
			sName = strsub(nName, p1);
			if ( NuNData[locals.itmIndex_dbKey][sName] ) then
				sName = ( NuNData[locals.itmIndex_dbKey][sName] );
			end
			if ( ( sName ) and ( ( NuNDataRNotes[sName] ) or ( NuNDataANotes[sName] ) ) ) then
				NuN_CreateIndex(nKey, sName);
			end
		end
	end
end

function NuN_CreateIndex(nKey, nName)
	if ( not NuNData[locals.mrgIndex_dbKey][nKey] ) then
		NuNData[locals.mrgIndex_dbKey][nKey] = {};
		NuNData[locals.mrgIndex_dbKey][nKey].noteCounter = 1;
		NuNData[locals.mrgIndex_dbKey][nKey][nName] = "1";
--		NuN_Message("-> MapNote : "..nName);
	elseif ( ( NuNData[locals.mrgIndex_dbKey][nKey] ) and ( NuNData[locals.mrgIndex_dbKey][nKey].noteCounter ) and  ( NuNData[locals.mrgIndex_dbKey][nKey].noteCounter < 5 ) and ( not NuNData[locals.mrgIndex_dbKey][nKey][nName] ) ) then
		NuNData[locals.mrgIndex_dbKey][nKey].noteCounter = NuNData[locals.mrgIndex_dbKey][nKey].noteCounter + 1;
		NuNData[locals.mrgIndex_dbKey][nKey][nName] = "1";
--		NuN_Message("-> MapNote : "..nName);
	end
end

function NuN_getFaction()
	local_player.factionName = UnitFactionGroup("player");
	if ( local_player.factionName ) then
		if ( local_player.factionName == "Horde" ) then
			NuN_horde = true;
		else
			NuN_horde = false;
		end
	else
		NuN_horde = nil;
	end
end



function NuN_ResetFriendlyData(resetTo, targetRealm)
	for realmName, realm in pairs(NuNData) do
		if ( ( not targetRealm ) or ( realmName == targetRealm ) ) then
			for playerName, playerData in pairs(realm) do
				if ( type(playerData) == "table" ) then
					if ( playerData.faction ) then
						if ( playerData.friendLst ) then
							playerData.friendLst = resetTo;
						end
						if ( playerData.ignoreLst ) then
							playerData.ignoreLst = resetTo;
						end
					end
				end
			end
		end
	end
end



-- Stop NuN trying to Ignore a Player (who never seems to be online for example)
function NuN_ResetPlayerIgnoreStatus(parm1)
	if ( ( locals.NuNDataPlayers[parm1] ) and ( locals.NuNDataPlayers[parm1].ignoreLst ) ) then
		if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[parm1].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[parm1].friendLst ) ) then
			locals.NuNDataPlayers[parm1] = nil;
		else
			locals.NuNDataPlayers[parm1].ignoreLst = nil;
		end
		NuNSettings[local_player.realmName].gNotIgnores[parm1] = true;
		if ( NuNSettings[local_player.realmName].autoA ) then
			ignoresPendingUpdate = ignoresPendingUpdate or NuN_Update_Ignored();
		end
		NuN_Message( NUN_FINISHED_PROCESSING .. " : " .. parm1 );
	end
end

-- Stop NuN trying to Befriend a Player (who isn't on the server any more for example)
function NuN_ResetPlayerFriendStatus(parm1)
	if ( ( locals.NuNDataPlayers[parm1] ) and ( locals.NuNDataPlayers[parm1].friendLst ) ) then
		if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[parm1].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[parm1].ignoreLst ) ) then
			locals.NuNDataPlayers[parm1] = nil;
		else
			locals.NuNDataPlayers[parm1].friendLst = nil;
		end
		NuNSettings[local_player.realmName].gNotFriends[parm1] = true;
		if ( NuNSettings[local_player.realmName].autoA ) then
			friendsPendingUpdate = friendsPendingUpdate or NuN_Update_Friends();
		end
		NuN_Message( NUN_FINISHED_PROCESSING .. " : " .. parm1 );
	end
end

-- reset Friend/Ignore lists to those of Current Toon
function NuN_ResetFriendIgnoreLists()
	NuN_ResetFriendlyData(nil, local_player.realmName);
	NuNSettings[local_player.realmName].gNotFriends = {};
	NuNSettings[local_player.realmName].gNotIgnores = {};
	NuNOptions_Export();					-- NOT a NuN Export function - this exports Blizzard Friend / Ignores to NotesUNeed notes
	NuN_Message( NUN_FINISHED_PROCESSING );
end




function NuNOptions_DBBackUp_OnClick()
	local isLoaded = IsAddOnLoaded("NotesUNeed_Backup");
	if ( not isLoaded ) then
		isLoaded = LoadAddOn("NotesUNeed_Backup");
	end
	if ( isLoaded ) then
		StaticPopup_Show("NUN_CONFIRM_BACKUP");
	end
end

function NuNOptions_DBRestore_OnClick()
	local isLoaded = IsAddOnLoaded("NotesUNeed_Backup");
	if ( not isLoaded ) then
		isLoaded = LoadAddOn("NotesUNeed_Backup");
	end
	if ( isLoaded ) then
		StaticPopup_Show("NUN_CONFIRM_RESTORE");
	end
end



function NuNOptions_DBImport()
	if ( NuN_ImportModule ) then
		StaticPopup_Show("NUN_IMPORT_NOTES");
	end
end

function NuNOptions_DBExport()
	if ( not NuNSearchFrame.qh ) then
		local isLoaded = IsAddOnLoaded("NotesUNeed_Export");
		if ( not isLoaded ) then
			isLoaded = LoadAddOn("NotesUNeed_Export");
		end
		if ( isLoaded ) then
			NuNSearchFrame.foundNuN = locals.foundNuN;
			NuNSearchFrame.local_player.realmName = local_player.realmName;
			StaticPopup_Show("NUN_CONFIRM_EXPORT");
		end
	end
end

function NuN_PurgeExport()
	local isLoaded = IsAddOnLoaded("NotesUNeed_Export");
	if ( not isLoaded ) then
		isLoaded = LoadAddOn("NotesUNeed_Export");
	end
	if ( isLoaded ) then
		NuNDataExport = {};
	end
	NuN_Message( NUN_FINISHED_PROCESSING );
end



function NuN_MassDelete()
--	if ( not strfind(NuNSearchTitleText:GetText(), NUN_QUESTS_TEXT) ) then
		StaticPopup_Show("NUN_MASS_DELETE_CONFIRM");
--	end
end

-- Set up the Unit Right Click menu options for recording Player Rating : will create a note if necesary
function NuN_SetupRatings(initialSetup)
	local UnitPopupMenus = UnitPopupMenus;
	
	-- Player specified ratings - MUST create Brand New arrays so that Originals are not corrupted by changes and are available to "Reset" the values
	if ( not NuNSettings.ratings ) then
		NuNSettings.ratings = {};
		NuNSettings.ratingsT = {};
		NuNSettings.ratingsBL = {};
		for i, value in pairs(NUN_ORATINGS) do
			NuNSettings.ratings[i] = value;
		end
		for i, value in pairs(NUN_ORATINGS_TEXT) do
			NuNSettings.ratingsT[i] = value;
		end
		for i=1, maxRatings, 1 do
			NuNSettings.ratingsBL[i] = 0;
		end

	else
		NUN_PR_TP = NuNSettings.ratings[1];			
		NUN_PR_MB = NuNSettings.ratings[2];			
		NUN_PR_AO = NuNSettings.ratings[3];			
		NUN_PR_RB = NuNSettings.ratings[4];			
		NUN_PR_SA = NuNSettings.ratings[5];			
		NUN_PR_LW = NuNSettings.ratings[6];        	
		NUN_PR_RR = NuNSettings.ratings[7];          
		NUN_PR_MG = NuNSettings.ratings[8];        	
		NUN_PR_DJ = NuNSettings.ratings[9];			
		NUN_PR_NJ = NuNSettings.ratings[10];			
		NUN_PR_SW = NuNSettings.ratings[11];			
		NUN_PR_NG = NuNSettings.ratings[12];
		NUN_PR_FG = NuNSettings.ratings[13];
		NUN_PR_AV = NuNSettings.ratings[14];
		NUN_PR_KS = NuNSettings.ratings[15];
		NUN_PR_TENSTAR = NuNSettings.ratings[16];
		NUN_PR_NINESTAR = NuNSettings.ratings[17];
		NUN_PR_EIGHTSTAR = NuNSettings.ratings[18];
		NUN_PR_SEVENSTAR = NuNSettings.ratings[19];
		NUN_PR_SIXSTAR = NuNSettings.ratings[20];
		NUN_PR_FIVESTAR = NuNSettings.ratings[21];
		NUN_PR_FOURSTAR = NuNSettings.ratings[22];
		NUN_PR_THREESTAR = NuNSettings.ratings[23];
		NUN_PR_TWOSTAR = NuNSettings.ratings[24];
		NUN_PR_ONESTAR = NuNSettings.ratings[25];
		NUN_PR___ = NuNSettings.ratings[26];
	end

	UnitPopupMenus["NUN_POPUP"] 	= {}
	UnitPopupButtons["NUN_POPUP"] 	= { text = TEXT(NUN_POPUP_TITLE), dist = 0, nested = 1, notClickable = 1 };
	if ( initialSetup ) then
		local menuItemCount = getn(UnitPopupMenus["RAID"]) + 1;
		local insertIndex = menuItemCount + 2;
		if ( UnitPopupMenus["RAID"][menuItemCount] == "CANCEL" ) then
			UnitPopupMenus["RAID"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["RAID"][insertIndex] = "NUN_POPUP";


		menuItemCount = getn(UnitPopupMenus["PARTY"]);
		insertIndex = menuItemCount + 1;
		if ( UnitPopupMenus["PARTY"][menuItemCount] == "CANCEL" ) then
			UnitPopupMenus["PARTY"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["PARTY"][insertIndex] = "NUN_POPUP";
		
		menuItemCount = getn(UnitPopupMenus["FOCUS"]);
		insertIndex = menuItemCount + 1;
		if UnitPopupMenus["FOCUS"][menuItemCount] == "CANCEL" then
			UnitPopupMenus["FOCUS"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["FOCUS"][insertIndex] = "NUN_POPUP";

		menuItemCount = getn(UnitPopupMenus["PLAYER"]);
		insertIndex = menuItemCount + 1;
		if ( UnitPopupMenus["PLAYER"][menuItemCount] == "CANCEL" ) then
			UnitPopupMenus["PLAYER"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["PLAYER"][insertIndex] = "NUN_POPUP";
		
		menuItemCount = getn(UnitPopupMenus["RAID_PLAYER"]);
		insertIndex = menuItemCount + 1;
		if ( UnitPopupMenus["RAID_PLAYER"][menuItemCount] == "CANCEL" ) then
			UnitPopupMenus["RAID_PLAYER"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["RAID_PLAYER"][insertIndex] = "NUN_POPUP";

		menuItemCount = getn(UnitPopupMenus["FRIEND"]);
		insertIndex = menuItemCount + 1;
		if ( UnitPopupMenus["FRIEND"][menuItemCount] == "CANCEL" ) then
			UnitPopupMenus["FRIEND"][insertIndex] = "CANCEL";
			insertIndex = menuItemCount;
		end
		UnitPopupMenus["FRIEND"][insertIndex] = "NUN_POPUP";

		hooksecurefunc("UnitPopup_ShowMenu", NuNNew_UnitPopup_ShowMenu);	-- 5.40
	end
end

--[[
dropdownMenu:	name of the frame that was clicked on (probably matches the name of unit)
frame_tag:		vehicle, self, target, party, boss, etc.
unit_tag:		party, arena, raid, pet#, etc.
u_name			should always be the name of the unit clicked on
userData:		used differently by all the code that actually passes a value for this param.

original signature:
UnitPopup_ShowMenu (dropdownMenu, which, unit, name, userData)
--]]
NuNNew_UnitPopup_ShowMenu = function(--[[table]]dropdownMenu, --[[string]]frame_tag, --[[string]]unit_tag, --[[string]]u_name, --[[int]]userData)
--[[
nun_msgf("dropdownMenu:%s  (%s)   frame_tag:%s  unit_tag:%s  u_name:%s  userdata:%s  UIDROPDOWNMENU_MENU_VALUE:%s   UIDROPDOWNMENU_INIT_MENU:%s    UIDROPDOWNMENU_MENU_LEVEL:%s",
	tostring(dropdownMenu.name), type(dropdownMenu), tostring(frame_tag), tostring(unit_tag), tostring(u_name), tostring(userData),
	tostring(UIDROPDOWNMENU_MENU_VALUE), tostring(UIDROPDOWNMENU_INIT_MENU.name), tostring(UIDROPDOWNMENU_MENU_LEVEL));
--]]
	u_name = UIDROPDOWNMENU_INIT_MENU.name;
	
	if ( "NUN_POPUP" == UIDROPDOWNMENU_MENU_VALUE ) then
		-- create the "Open Note" item and add it to the submenu at the first position
		local info = UIDropDownMenu_CreateInfo();
		info.text = TEXT(NUN_POPUP_TOGGLE);
		info.owner = UIDROPDOWN_MENU_VALUE;
		info.checked = nil;
		info.value = NUN_POPUP_TOGGLE;
		info.func = NuNNew_UnitPopup_OnClick;
		info.notCheckable = 1;
		UIDropDownMenu_AddButton(info, UIDROPDOWNMENU_MENU_LEVEL);

		NuN_BuildPlayerRatingsSubmenu(NuNNew_UnitPopup_OnClick, u_name, UIDROPDOWNMENU_MENU_VALUE);
	end

end

-- 5.60 Re-Written
NuNNew_UnitPopup_OnClick = function(self)
	local btn = self.value;
	local NuN_PlayerRated = nil;
	local rating = nil;

	if ( btn == NUN_POPUP_TOGGLE ) then
		-- open a NuN Note for the Player
		rating = 99;
	else
		for i=1, maxRatings, 1 do							-- 5.60 BETA.02	leave if no NuN button clicked
			if ( btn == NuNSettings.ratings[i] ) then	-- 5.60 BETA.02	leave if no NuN button clicked
				NuN_PlayerRated = true;
				rating = i;
				break;								-- 5.60 BETA.02	leave if no NuN button clicked
			end										-- 5.60 BETA.02	leave if no NuN button clicked
		end											-- 5.60 BETA.02	leave if no NuN button clicked
	end

	if ( not rating ) then return; end

--nun_msgf("NuNNew_UnitPopup_OnClick - self:%s   btn:%s  NuN_PlayerRated:%s  rating:%s", tostring(self:GetName()), tostring(btn), tostring(NuN_PlayerRated), tostring(rating));
	local dropdownFrame = UIDROPDOWNMENU_INIT_MENU;
	if ( type(dropdownFrame) == "string" ) then
		dropdownFrame = _G[dropdownFrame];
	end
	local _name = dropdownFrame.name;
	local server = dropdownFrame.server;
	local unit = dropdownFrame.unit;

	if ( ( server ) and ( server ~= "" ) and ( server ~= local_player.realmName ) ) then
-- Ignore Players from other Realms.... ? Concatenate Server-Name.... ? Try to fetch Name from other Realm Player data.... ?
--		NuN_Message("NuN : Player " .. _name .. " from different Realm : " .. server);
		return;		-- Ignore for now...
	end

	-- simplifies the readability of the "if" tests later on...
	local leaveOpen = nil;
	if ( ( not NuN_PlayerRated ) or ( IsAltKeyDown() ) ) then
		leaveOpen = true;
	end

	-- if player note already exists
	if ( locals.NuNDataPlayers[_name] ) then
		if ( unit ) then
			-- not currently updating existing notes with double checks of data
			-- but if I want to, then this is the place to do it
		end
		if ( leaveOpen ) then
			NuN_ShowSavedNote(_name);
		end

	-- else have to create a note via some method
	else
		if ( ( unit ) and ( unit == "target" ) ) then
			if ( leaveOpen ) then
				NuN_FromTarget(false);
			else
				NuN_FromTarget(true);
			end

		elseif ( unit ) then
			NuN_NewContact(unit);
			if ( not leaveOpen ) then
				NuN_WriteNote();
				HideNUNFrame();
				NuN_Message(local_player.currentNote.unit..NUN_AUTONOTED);
			end

		else
			NuN_CreateContact(_name, local_player.factionName);	-- fingers crossed not an opposing faction name ??? "target" taken care of above, but what about chat links ?
			if ( not leaveOpen ) then
				NuN_WriteNote();
				HideNUNFrame();
				NuN_Message(local_player.currentNote.unit..NUN_AUTONOTED);
			end
		end
	end

	if ( ( NuN_PlayerRated ) and ( rating ~= 99 ) ) then
		if ( locals.NuNDataPlayers[_name] ) then
			locals.NuNDataPlayers[_name].prating = rating;
			pRating = rating;
--			NuN_Message("Updated player rating for " .. _name);
		end
		if ( _name == local_player.currentNote.unit ) then
			locals.dropdownFrames.ddPRating = rating;
			contact.prating = NuNSettings.ratings[locals.dropdownFrames.ddPRating];
			UIDropDownMenu_SetSelectedID(NuNPRatingDropDown, locals.dropdownFrames.ddPRating);
			UIDropDownMenu_SetText(NuNPRatingDropDown, contact.prating);
		end
		if ( BlackList ) then
			NuN_BlackList(_name, rating);
		end
	end

	DropDownList1:Hide();
end

function NuN_BlackList(_name, rating)
	local r = NuNSettings.ratingsBL[rating];
	
	if ( r > 0 ) then
		local reason = "|cffA335ED[NuN]: |r" .. NuNSettings.ratings[rating] .. "\n" .. NuNSettings.ratingsT[rating];
		local ignore, warn = false, false;
		if ( ( r == 2 ) or ( r == 4 ) ) then
			ignore = true;
		end
		if ( r > 2 ) then
			warn = true;
		end
		local blI = BlackList:GetIndexByName(_name);
		if ( blI > 0 ) then
			local player = BlackList:GetPlayerByIndex(blI);
			local l, c, r = player.level, player.class, player.race;
			NuN_BLCheckBox_Ignore:SetChecked(ignore);
			NuN_BLCheckBox_Warn:SetChecked(warn);
			ignore = NuN_BLCheckBox_Ignore;
			warn = NuN_BLCheckBox_Warn;
			BlackList:UpdateDetails(blI, ignore, warn, reason, l, c, r);
		else
			BlackList:AddPlayer(_name, warn, ignore, reason);
		end
	end

	StaticPopup1:Hide();
end

function NuN_EnableBL()
	if ( NuN_BLCheckBox:GetChecked() ) then
		NuN_BLCheckBox_Ignore:Show();
		NuN_BLCheckBox_Warn:Show();
	else
		NuN_BLCheckBox_Ignore:Hide();
		NuN_BLCheckBox_Warn:Hide();
	end
end

function NotesUNeed.NuN_Statics.ToggleDebugMode()
	locals.NuNDebug = not locals.NuNDebug;
	if locals.NuNDebug then
		nun_msgf("Debug mode is now enabled.");
	else
		nun_msgf("Debug mode is now disabled.");
	end
	NuNSettings[local_player.realmName].debugMode = locals.NuNDebug;
end

function NotesUNeed.NuN_Statics.ToggleAddMessageHandler()
	locals.processAddMessage = not locals.processAddMessage;
	if locals.processAddMessage then
		NuN_Message("NotesUNeed is now processing calls to the chat frame's AddMessage() function.");
	else
		NuN_Message("NotesUNeed will stop processing calls to the chat frame's AddMessage() function.");
	end
	
	NuNSettings[local_player.realmName].processChat = locals.processAddMessage;
end

function NotesUNeed.NuN_Statics.NuNNew_AddMessage(chatframe, msg, r, g, b, id, ...)
	if not chatframe.processingMsg then
		chatframe.processingMsg = true;

		if ( ( msg ) and ( type(msg) == "string" ) ) then
			local altmsg = "";
--[[
local arg1, arg2, arg3, arg4 = ...
if strfind(msg, "Narwinslave") then
nun_msgf("arg1:%s   arg2:%s   arg3:%s   arg4:%s", tostring(arg1), tostring(arg2), tostring(arg3), tostring(arg4));
end
--]]
			if ( ( NuNSettings ) and ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].chatty ) ) then		-- 20200
				for before, playerLink, after in strgmatch(msg, "(.*)(%b[])(.*)") do
					local checkName = strsub(playerLink, 2, strlen(playerLink) - 1);
					local dash = strfind(checkName, "-");
					if ( dash ) then
						checkName = strsub(msg, 1, dash-1);
					end
					if ( checkName ) then
						checkName = NuN_StripColorCode(checkName);
--[[						
if strfind(msg, "Narwinslave") then
nun_msgf("checkName:%s   locals.NuNDataPlayers[%s]:%s      locals.NuNDataPlayers[Narwinslave]:%s", checkName, strgsub(checkName, "\124", "\124\124"), tostring(locals.NuNDataPlayers[checkName]), tostring(locals.NuNDataPlayers["Narwinslave"]));
end
--]]
						if ( locals.NuNDataPlayers[checkName] ) and true then--( checkName ~= locals.player_Name ) ) then
							local colorOpen, colorClose = "|cffA336ED", "|r";
							local linkOpen, linkClose = "|HNuN:" .. checkName .. "|h", "|h";
							local displayText = "{";
							if ( locals.NuNDataPlayers[checkName].prating ) then
								local rating = NuNSettings.ratings[locals.NuNDataPlayers[checkName].prating];
								if ( ( rating ~= "" ) and ( rating ~= " " ) ) then
									displayText = displayText .. rating;
								end
							end
							displayText = displayText .. "}";
							if ( displayText == "{}" ) then
								displayText = "";
							else
								displayText = " " .. displayText;
							end
							
							-- now insert the icon
							local noteIcon = "";
							if locals.NuNDataPlayers[checkName].faction == "Horde" then
								noteIcon = "|TInterface\\AddOns\\NotesUNeed\\Artwork\\tHorde.blp:32|t";
							elseif locals.NuNDataPlayers[checkName].faction == "Alliance" then
								noteIcon = "|TInterface\\AddOns\\NotesUNeed\\Artwork\\tAlliance.blp:32|t";
							end
							if displayText == "" and noteIcon ~= "" then
								-- if we have a note for this player, but haven't assigned them a player rating, the icon will act as the hotspot for the hyperlink
								displayText = noteIcon;
								noteIcon = "";
							end
							
							if strsub(after, 1, 3) == "|h" then
								after = strsub(after, 3);
							end
							
							if displayText ~= "" and noteIcon ~= "" then
								msg = before .. playerLink .. "|h" .. colorOpen .. linkOpen .. displayText .. linkClose .. colorClose .. linkOpen .. noteIcon .. linkClose .. after;
							elseif noteIcon == "" then
								msg = before .. playerLink .. "|h" .. colorOpen .. linkOpen .. displayText .. linkClose .. colorClose .. noteIcon .. after;
							end
							
							break;
						end
					end
				end
			end

--			msg = altmsg..msg;
		end
		if locals.NuNDebug and chatframe == DEFAULT_CHAT_FRAME and locals.debugging_msg_hooks then
			chatframe.NuNOri_AddMessage(chatframe, "msg:" .. strgsub(msg, "\124", "\124\124"), r, g, b, id, ...);
		end
		chatframe.NuNOri_AddMessage(chatframe, msg, r, g, b, id, ...);
		chatframe.processingMsg = nil;
	elseif chatframe.printingDebugMessage then
		chatframe.NuNOri_AddMessage(chatframe, msg, r, g, b, id, ...);
	end
end

function NotesUNeed.NuN_Statics.TagPlayerChatName( playerChatName, showDebug )
	local noteIcon, taggedPlayerName = nil, playerChatName;
	if playerChatName and type(playerChatName) == "string" then
		if locals.NuNDebug and showDebug then
			print(string.format("NotesUNeed.NuN_Statics.TagPlayerChatName - playerChatName:%s (%s)", playerChatName, strgsub(playerChatName, "\124", "\124\124")));
		end
		
		if (not locals.processAddMessage) and NuNSettings and NuNSettings[local_player.realmName] and NuNSettings[local_player.realmName].chatty then
			-- strip the color code from the name (but remember the color code), because our table is indexed by player name with no color codes
			local strippedPlayerName = NuN_StripColorCode(playerChatName);
			-- now take this value and apply it
			if locals.NuNDataPlayers[strippedPlayerName] and true then--playerChatName ~= locals.player_Name then
				if locals.NuNDataPlayers[strippedPlayerName].faction == "Horde" then
					noteIcon = "|TInterface\\AddOns\\NotesUNeed\\Artwork\\tHorde.blp:0|t";
				elseif locals.NuNDataPlayers[strippedPlayerName].faction == "Alliance" then
					noteIcon = "|TInterface\\AddOns\\NotesUNeed\\Artwork\\tAlliance.blp:0|t";
				end
				
				--this works, but not exactly the way I'd like...
				if locals.NuNDebug and showDebug then
					nun_msgf("    ----  tagging player name %s  with noteIcon %s", strippedPlayerName, tostring(noteIcon));
				end

				local colorOpen, colorClose = "|cffA336ED", "|r";
				local linkOpen, linkClose = "|H:NuN:" .. strippedPlayerName .. "|h", "|h";
				local displayText = "{";
				if ( locals.NuNDataPlayers[strippedPlayerName].prating ) then
					local rating = NuNSettings.ratings[locals.NuNDataPlayers[strippedPlayerName].prating];
					if ( ( rating ~= "" ) and ( rating ~= " " ) ) then
						displayText = displayText .. rating;
					end
				end
				if not noteIcon then
					noteIcon = "";
				end
				displayText = displayText .. "}";
				if ( displayText == "{}" ) then
					displayText = "";
				else
					displayText = " " .. displayText;
				end
				taggedPlayerName = colorOpen .. linkOpen .. strippedPlayerName .. displayText .. linkClose .. colorClose .. noteIcon;
			end
		end
	end
	return taggedPlayerName;--prefix .. playerChatName;
end


-- This function records the player location in a new QST type note when Objectives are completed so you know for next time
function NuN_QuestWatch_Update()
	local numObjectives;
	local text, type, finished;
	local questIndex;

	if ( ( not local_player.realmName ) or ( not locals.NuNDataPlayers ) ) then
		return;
	end

	for i=1, GetNumQuestWatches() do
		questIndex = GetQuestIndexForWatch(i);

		if ( questIndex ) then
			local theQuest = GetQuestLogTitle(questIndex);

			if ( theQuest ) then
				local qNoteLevel = nil;

				if ( NuNDataANotes[theQuest] ) then
					qNoteLevel = "Account";

				elseif ( NuNDataRNotes[theQuest] ) then
					qNoteLevel = "Realm";
				end

				if ( ( qNoteLevel ) and ( NuNSettings[local_player.realmName].autoQ ) ) then
					local l_c_note = local_player.currentNote.general;
					local l_g_text = general.text;
					local l_c_name = local_player.currentNote.unit;

					local theQuestI = theQuest..":"..NUN_OBJECTIVES;
					local_player.currentNote.general = theQuestI;
					if ( ( not NuNDataANotes[theQuestI] ) and ( not NuNDataRNotes[theQuestI] ) ) then
						if ( qNoteLevel == "Account" ) then
							NuNDataANotes[theQuestI] = {};
							NuNDataANotes[theQuestI].type = 5;
						else
							NuNDataRNotes[theQuestI] = {};
							NuNDataRNotes[theQuestI].type = 5;
						end
						general.text = " ";
						NuNF.NuN_SetGText(qNoteLevel);

					elseif ( NuNDataANotes[theQuestI] ) then
						qNoteLevel = "Account";
						general.text = NuNF.NuN_GetGText(local_player.currentNote.general);

					elseif ( NuNDataRNotes[theQuestI] ) then
						qNoteLevel = "Realm";
						general.text = NuNF.NuN_GetGText(local_player.currentNote.general);
					end

					numObjectives = GetNumQuestLeaderBoards(questIndex);

					local location = nil;
					if ( numObjectives > 0 ) then
						for j=1, numObjectives do
							text, type, finished = GetQuestLogLeaderBoard(j, questIndex);
							if ( ( not strfind(general.text, text) and ( not strfind(text, ": 0") ) and ( strlen(general.text) < NuNC.NUN_MAX_TXT_BUF ) ) ) then
								if ( not location ) then
									SetMapToCurrentZone();
									location = NuNF.NuN_GetLoc();
									if ( location ) then
										location = NuN_LocStrip(location);
									end
								end
								if ( location ) then
									general.text = general.text.."\n"..location.."\n"..text.."\n";
									NuNF.NuN_SetGText(qNoteLevel);
								end
							end
						end
					end

					local_player.currentNote.general = l_c_note;
					general.text = l_g_text;
					local_player.currentNote.unit = l_c_name;
				end
			end
		end
	end
end

local __OnShowGameTooltip = NuN_GameTooltip_OnShow
function NuN_MainUpdate(self,elapsed)
	-- delay harvesting NPC information from a tooltip, until the tooltip is actually visible
	if ( ( NPCInfo_Proceed ) and ( GameTooltip:IsVisible() ) ) then
		NPCInfo_Proceed.func( NPCInfo_Proceed.autoHide );
		return;
	end
	-- In case of unforseen circumstance; Make sure the WhoFrame doesn't remain UnRegistered for more than a maximum of  (NuNC.NUN_WHO_TIMELIMIT * 2)  seconds
	if ( NuN_WhoReturnStruct.timeLimit ) then
		NuN_WhoReturnStruct.timeLimit = NuN_WhoReturnStruct.timeLimit + elapsed;
		if ( NuN_WhoReturnStruct.timeLimit > (NuNC.NUN_WHO_TIMELIMIT * 2) ) then
					NuN_WhoReturnStruct.func = nil;													-- 5.60
					NuN_WhoReturnStruct.name = nil;													-- 5.60
					NuN_WhoReturnStruct.timeLimit = nil;											-- 5.60
					NuN_WhoReturnStruct.secondTry = nil;
					NuN_suppressExtraWho = nil;
					if ( ( NuNSettings[local_player.realmName] ) and ( NuNSettings[local_player.realmName].alternativewho ) ) then
						SetWhoToUI(0);                                          					-- 5.60
						FriendsFrame:RegisterEvent("WHO_LIST_UPDATE");								-- 5.60
					end

		elseif ( ( NuN_WhoReturnStruct.timeLimit > NuNC.NUN_WHO_TIMELIMIT ) and ( not NuN_WhoReturnStruct.secondTry ) ) then
			NuN_WhoReturnStruct.secondTry = true;
			NuN_suppressExtraWho = true;
			SendWho("n-"..NuN_WhoReturnStruct.name);								-- 1 more try, as perhaps the first was ignored by Blizzard Timer
--			NuN_WhoReturnStruct.timeLimit = 9999;
		end
	end

	if ( NuN_DTrans.Status == "Sending" ) then
		NuN_DTrans.tTrack = NuN_DTrans.tTrack + elapsed;
		if ( NuN_DTrans.tTrack > NuN_DTrans.tDelay ) then
			NuN_DTrans.tTrack = 0;
			NuN_DTrans.aIndex = NuN_DTrans.aIndex + 1;
			if ( NuN_DTrans.pArray[ NuN_DTrans.aIndex ] ) then
					SendChatMessage(	NuN_DTrans.pArray[ NuN_DTrans.aIndex ], 
										NuN_DTrans.Params.sendTo, 
										NuN_DTrans.Params.dfltLang, 
										NuN_DTrans.Params.user	);
			else
				NuN_DTrans.Status = "Inactive";
				NuN_DTrans.aIndex = 0;
				NuN_DTrans.pArray = {};
				NuN_DTrans.Prefix = "";
			end
		end
	end

	-------------------------- 5.60 Moved from MicroButton OnUpdate --------------------------------
	if ( ( local_player.factionName == nil ) or ( NuN_horde == nil ) ) then
		NuN_getFaction();
	end

	-- needed to introduce a delay at startup before Friends/Ignores could be refreshed... not sure if still necessary
	if ( ( friendsPendingUpdate ) or ( ignoresPendingUpdate ) ) then
		NuN_AttemptedFriendIgnores = 0;

		if ( friendsPendingUpdate ) then
			if ( GetNumFriends() > 0 ) then
				friendsPendingUpdate = NuN_Update_Friends();
			else
				friendsPendingUpdate = nil;
			end
		end

		if ( ignoresPendingUpdate ) then
			if ( GetNumIgnores() > 0 ) then
				ignoresPendingUpdate = NuN_Update_Ignored();
			else
				ignoresPendingUpdate = nil;
			end
		end
	end

	-- accounts for the delay between tooltip SetLink and display of the actual ItemRef tooltip.....
	if ( ( delayedItemTooltip ) and ( ItemRefTooltip:IsVisible() ) ) then
		NuNF.NuN_GNoteFromItem(delayedItemTooltip, "ItemRefTooltip");
		delayedItemTooltip = nil;
		ItemRefTooltip:Hide();
	end

	-- delayed display of NuN tooltip until we have something to anchor it to
	if ( locals.noTipAnchor ) then
		__OnShowGameTooltip(self, locals.noTipAnchor);
	end

	-- If receiving a note, and not waiting for a decision from the player on a possible Duplicate Record
	if ( ( locals.NuN_Receiving.active ) and ( not receiptPending ) ) then
		locals.NuN_Receiving.timer = locals.NuN_Receiving.timer + elapsed;
		if ( locals.NuN_Receiving.timer > receiptDeadline ) then
			if ( locals.NuN_Receiving.version ) then
				NuN_Message(NUN_RECEIPT_EXPIRED..NuN_Receiving.from);	-- Final parts of the message didn't arrive in a reasonable amount of time, so gave up on receipt process
				NuN_WriteReceiptLog();
			end
			locals.NuN_Receiving = {};
			receiptDeadline = defaultReceiptDeadline;
		end
	end

	-- This stops the user from double clicking the Transmit button, and sending multiple notes in quick succession
	if ( busySending.active ) then
		NuN_transmissionTimer = NuN_transmissionTimer - elapsed;
		if ( NuN_transmissionTimer < 0.1 ) then
			busySending = {};
			busySending.user = "";
			busySending.active = nil;
			busySending.counter = 0;
			NuN_transmissionTimer = 0;
			NuNTransmit:Enable();
		end
	end

	if ( NuN_State.togglePinUp ) then
		NuN_FlagMoved();
		NuN_ClearPinnedTT();
		NuN_PinnedTooltipToggle(self, NuN_PinnedTooltip.type, NuN_PinnedTooltip.noteName);
		NuN_State.togglePinUp = false;
	end
	
	-- Mainly a timed update of Quest notes
	locals.timeSinceLastUpdate = locals.timeSinceLastUpdate + elapsed;
	if ( ( locals.timeSinceLastUpdate > 3 ) and ( not UnitAffectingCombat("player") ) ) then

		if ( not locals.NuNDataPlayers[locals.player_Name] ) then
			NuN_AutoNote();
		end
		
		
		-- OnUpdate - CALL TO GUILD ROSTER
		if ( NuN_State.NuN_syncGuildMemberNotes and locals.NuNDebug ) then
			nun_msgf("MAIN FRAME UPDATE - syncGuildNotes:%s", tostring(NuN_State.NuN_syncGuildMemberNotes));
		end
		-- this means that the calendar has already loaded and we've removed our protective hook
		if ( NuN_State.NuN_syncGuildMemberNotes ) then
			GuildRoster();
		end
		
		if ( NuN_State.oneDone ) then
			NuN_State.oneDone = false;
			NuN_State.NuN_AtStartup = false;
			NuN_State.NuN_QuestsUpdating = false;

		else
			NuN_State.oneDone = true;
			--[[
			if ( NuN_QLF and not NuN_QLF:IsVisible() ) then
				NuNF.NuN_CheckQuestList();
				if ( not NuN_State.NuN_IgnoreNextQUpdate ) then
					NuNF.NuN_UpdateQuestNotes("Timed");
				end
			end
			--]]
		end
		locals.timeSinceLastUpdate = 0;
	end
	-------------------------- 5.60 Moved from MicroButton OnUpdate --------------------------------

	if ( sn.totCount > 0 ) then
		if ( not sn.Waiting ) then
			sn.Count = sn.Count + 1;
			if ( sn.Array[sn.Count] ) then
				NuN_ProcessSocialNote( sn.Array[sn.Count] );
			else
				NuN_ImportSocialNote_Terminate();
			end
		end
	end

end



function NuN_CustomFontCheckBox_OnClick(self,btn,down)
	if ( self:GetChecked() ) then
		NuN_UpdateFont(NuNC.NUN_FONT1, 12);
		NuNSettings[local_player.realmName].nunFont = true;
	else
		NuN_UpdateFont("Fonts\\FRIZQT__.TTF", 12);
		NuNSettings[local_player.realmName].nunFont = nil;
	end
end

function NuN_UpdateFont(theFont, fontSize)
	if ( theFont ) then
		NuNText:SetFont(theFont, fontSize);
		NuNEditDetailsBox:SetFont(theFont, fontSize);
		NuNOptionsTTLengthTextBox:SetFont(theFont, fontSize);
		NuNOptionsTTLineLengthTextBox:SetFont(theFont, fontSize);
		NuNChatTextBox:SetFont(theFont, fontSize);
		NuNSearchTextBox:SetFont(theFont, fontSize);
		NuNGNoteTextBox:SetFont(theFont, fontSize);
		NuNGNoteTextScroll:SetFont(theFont, fontSize);
	end
end


----------------------------------------------------------------------------------------------------------------------------------------------
--[[	NuN_FindGuildMemberNotes
  **
  ** Searches the notes database for notes on players belonging to the specified guild.
  **
  ** @param		[string]	the name of the guild to search for members of.
  **
  ** @return	[int]		number of elements in the table
  **			[table]		indexed by the guild member's name, a list of the members from the
  **						specified guild for which we have notes for.
--]]
function NuN_FindGuildMemberNotes( guildName )
	local foundCount, guildMembers = 0, {};
	for _name, details in pairs(locals.NuNDataPlayers) do
		if ( details.guild and details.guild == guildName ) then
			guildMembers[_name] = true;
			foundCount = foundCount + 1;
		end
	end
	
	return foundCount, guildMembers;
end

function NuN_SyncGuildMemberNotes( forceReport )
--	if ( NuNHooks.NuNOriginal_OpenCalendar ~= nil ) then
--		return;
--	end
	
	local numGuildMembers = GetNumGuildMembers(true);
	local guildName = GetGuildInfo("player");

	if ( locals.NuNDebug ) then
		nun_msgf("INITIALIZING GUILD ROSTER [NuN_SyncGuildMemberNotes(forceReport=%s)] guildName:%s    forceGR:%s    numGuildMembers:%i", tostring(forceReport), tostring(guildName), tostring(NuN_State.NuN_syncGuildMemberNotes), numGuildMembers);
	end

	if ( ( not guildName ) or ( guildName == "" ) ) then
		return;

	elseif ( numGuildMembers < 1 ) then
--evo:	it seems like we should just let MainUpdate call GuildRoster during the next frame, by setting NuN_syncGuildMemberNotes = true
--		so I need to find out why GuildRoster() is called here...
		GuildRoster();
		NuN_State.NuN_syncGuildMemberNotes = true;
		return;
	end

	local statsNoted, statsOnline, statsNew, statsX = 0, 0, 0, 0;
	local classes, notedMembers;
	if ( NuN_horde ) then
		classes = NUN_HCLASSES;
	else
		classes = NUN_ACLASSES;
	end

	-- Build array of Members we have already noted (may need to rely on .guild rather than User Definable...) i.e. do BEFORE updating notes from Guild Roster ;)
	statsNoted, notedMembers = NuN_FindGuildMemberNotes(guildName);

	-- Process the Guild Roster, updating notes, and noting Newly noted members (status=<AFK>, <DND>, etc.)
	local memberName, memberRank, memberRankIndex, memberClass, memberIsOnline, memberStatus,
		memberGuildAchievmentPoints, memberGuildAchievementRank, memberIsMobile, __;
	local rosterA = {};
	local hdr1, hdr2 = locals.pHead..1, locals.pHead..2;
	local phdr, pdtl;
	local dfltH1, dfltH2 = NUN_DFLTHEADINGS[1], NUN_DFLTHEADINGS[2];
	for guildMemberIndex = 1, numGuildMembers, 1 do
		memberName, memberRank, memberRankIndex, --[[playerLevel]]__, memberClass, --[[zone]]__, --[[public note]]__, --[[officerNote]]__, memberIsOnline, memberStatus,
			--[[englishClassName]]__, memberGuildAchievementPoints, memberGuildAchievementRank, memberIsMobile = GetGuildRosterInfo(guildMemberIndex);
		memberClass = NuNF.NuNGet_TableID(classes, memberClass);
		if ( memberName ) then
			rosterA[memberName] = {};
			rosterA[memberName].i = guildMemberIndex;
			if ( memberIsOnline ) then
				statsOnline = statsOnline + 1;
				rosterA[memberName].o = memberIsOnline;
			end
			if ( not locals.NuNDataPlayers[memberName] ) then
				-- no existing note for this player; treat them as a new guild member (even though we may be the one that just joined the guild)
				rosterA[memberName].n = true;
				statsNew = statsNew + 1;
				locals.NuNDataPlayers[memberName] = {};
				locals.NuNDataPlayers[memberName].type = NuNC.NUN_GUILD_C;
			else
				if ( locals.NuNDataPlayers[memberName].guild ) then
					-- make sure the note for this player is set to guild member
					if ( not locals.NuNDataPlayers[memberName].type ) then
						locals.NuNDataPlayers[memberName].type = NuNC.NUN_GUILD_C;
					end

					-- if our existing note for this member already has the correct guild (i.e. the same as the player's), make sure we're
					-- not treating them as a new member.
					if ( locals.NuNDataPlayers[memberName].guild == guildName ) then
						rosterA[memberName].n = nil;
					else
						-- this is the first time we're seeing this guild member, so set the guild in their player note and mark them as new
						locals.NuNDataPlayers[memberName].guild = guildName;
						rosterA[memberName].n = true;
						statsNew = statsNew + 1;
					end
				end
			end
			locals.NuNDataPlayers[memberName].faction = local_player.factionName;
			locals.NuNDataPlayers[memberName].guild = guildName;
			locals.NuNDataPlayers[memberName].cls = memberClass;
			-- if Player Specific Heading not equal to default, -OR- Account Specific Heading not equal to default, then don't update -ELSE-
			phdr = memberName..hdr1;
			if (	(locals.NuNDataPlayers[phdr] and locals.NuNDataPlayers[phdr].txt ~= dfltH1) or
					(NuNSettings[hdr1] and NuNSettings[hdr1].txt ~= dfltH1) ) then
				-- do nothing...
			else
				pdtl = memberName .. locals.pDetl .. "1";
				locals.NuNDataPlayers[pdtl] = {};
				locals.NuNDataPlayers[pdtl].txt = guildName;
				phdr = memberName..hdr2;
				if (	(locals.NuNDataPlayers[phdr] and locals.NuNDataPlayers[phdr].txt ~= dfltH2) or
						(NuNSettings[hdr2] and NuNSettings[hdr2].txt ~= dfltH2) ) then
					-- do nothing...
				else
					pdtl = memberName .. locals.pDetl .. "2";
					locals.NuNDataPlayers[pdtl] = {};
					if ( memberRankIndex == 0 ) then
						memberRank = ("GM : "..memberRank);
					else
						memberRank = (memberRankIndex.." : "..memberRank);
					end
					locals.NuNDataPlayers[pdtl].txt = memberRank;
				end
			end
		end
	end

	-- Check for members we noted as in the Guild previously, who are no longer in the Guild
	for member in pairs(notedMembers) do
		if ( not rosterA[member] ) then
			rosterA[member] = { x = true };
--			rosterA[member].x = true;
			statsX = statsX + 1;
		end
	end

	-- if forceReport mode, then report refresh details
	if ( ( forceReport ) and ( ( forceReport == "Startup" ) or ( statsX > 0 ) or ( statsNew > 0 ) ) ) then
		local catTxt, catSep, catCount = "", "", 0;
		NuN_Message( "NotesUNeed : " .. NuN_Strings.NUN_GUILD .. " - " .. numGuildMembers .. " (".. statsOnline .. ")" );
		NuN_Message( "NotesUNeed : " .. NUN_NEW_GUILDMATE .. " (" .. statsNew .. ") : " );
		for member, details in pairs(rosterA) do
			if ( details.n ) then
				catTxt = catTxt .. catSep .. member;
				catCount = catCount + 1;
				catSep = ", ";
			end
			if ( catCount > 9 ) then				-- output 10 names per line
				NuN_Message("    " .. catTxt);
				catTxt = "";
				catSep = "";
				catCount = 0;
			end
		end
		if ( catTxt ~= "" ) then
			NuN_Message("    " .. catTxt);
		end
		catTxt = "";
		catSep = "";
		catCount = 0;
		NuN_Message( "NotesUNeed : " .. NUN_X_GUILDMATE .. " (" .. statsX .. ") : " );
		for member, details in pairs(rosterA) do
			if ( details.x ) then
				catTxt = catTxt .. catSep .. member;
				catCount = catCount + 1;
				catSep = ", ";
			end
			if ( catCount > 9 ) then				-- output 10 names per line
				NuN_Message("    " .. catTxt);
				catTxt = "";
				catSep = "";
				catCount = 0;
			end
		end
	end

end										


-----------------------------------------------------------------------------------------------


function NuNOptions_SetModifier(mBttn)
	if ( IsModifierKeyDown() ) then
		local modKeys = "";
		local antiKey = "";
		local sep = "";
		local antiSep = "";

		if ( mBttn == "LeftButton" ) then

			if ( IsControlKeyDown() ) then
				modKeys = "Control";
				sep = "-";
			else
				antiKey = "Control";
				antiSep = "-";
			end
			if ( IsAltKeyDown() ) then
				modKeys = modKeys .. sep .. "Alt";
				sep = "-";
			else
				antiKey = antiKey .. antiSep .. "Alt";
				antiSep = "-";
			end
			if ( IsShiftKeyDown() ) then
				modKeys = modKeys .. sep .. "Shift";
			else
				antiKey = antiKey .. antiSep .. "Shift";
			end

		elseif ( mBttn == "RightButton" ) then

			if ( IsLeftControlKeyDown() ) then
				modKeys = "LeftControl";
				sep = "-";
			else
				antiKey = "LeftControl";
				antiSep = "-";
			end
			if ( IsRightControlKeyDown() ) then
				modKeys = modKeys .. sep .. "RightControl";
				sep = "-";
			else
				antiKey = antiKey .. antiSep .. "RightControl";
				antiSep = "-";
			end
			if ( IsLeftAltKeyDown() ) then
				modKeys = modKeys .. sep .. "LeftAlt";
				sep = "-";
			else
				antiKey = antiKey .. antiSep .. "LeftAlt";
				antiSep = "-";
			end
			if ( IsRightAltKeyDown() ) then
				modKeys = modKeys .. sep .. "RightAlt";
				sep = "-";
			else
				antiKey = antiKey .. antiSep .. "RightAlt";
				antiSep = "-";
			end
			if ( IsLeftShiftKeyDown() ) then
				modKeys = modKeys ..sep .. "LeftShift";
				sep = "-";
			else
				antiKey = antiKey .. antiSep .. "LeftShift";
				antiSep = "-";
			end
			if ( IsRightShiftKeyDown() ) then
				modKeys = modKeys ..sep .. "RightShift";
			else
				antiKey = antiKey .. antiSep .. "RightShift";
			end

		end
		NuNSettings[local_player.realmName].modKeys = modKeys;
		NuNSettings[local_player.realmName].antiKey = antiKey;
		NuNOptions_SetModifierText();

	else
		NuNSettings[local_player.realmName].mouseBttn = mBttn;
		NuNOptions_SetModifierText();
	end
end

function NuNOptions_SetModifierText()
	if ( ( NuNSettings ) and ( NuNSettings[local_player.realmName] ) ) then
		if ( not NuNSettings[local_player.realmName].modKeys or NuNSettings[local_player.realmName].modKeys == "" ) then
			NuNSettings[local_player.realmName].modKeys = "Alt";
		end
		if ( not NuNSettings[local_player.realmName].antiKey ) then
			NuNSettings[local_player.realmName].antiKey = "Control-Shift";
		end
		if ( not NuNSettings[local_player.realmName].mouseBttn ) then
			NuNSettings[local_player.realmName].mouseBttn = "LeftButton";
		end

		local modKeys = {};
		if ( strfind(NuNSettings[local_player.realmName].modKeys, "-") ) then
			modKeys = { strsplit("-", NuNSettings[local_player.realmName].modKeys) };
		else
			modKeys[1] = NuNSettings[local_player.realmName].modKeys;
		end
		local bttnText = "<" .. NUN_MODIFIERS[ modKeys[1] ] .. ">";
		for i=2, getn(modKeys), 1 do
			bttnText = bttnText .. "+<" .. NUN_MODIFIERS[ modKeys[i] ] .. ">";
		end
		bttnText = bttnText .. " : " .. NuNSettings[local_player.realmName].mouseBttn;
		NuNOptionsModifier:SetText(bttnText);
	else
		NuNOptionsModifier:SetText("<Alt> : LeftButton");
	end
end

function IsNuNModifierKeyDown(mBttn)
	if ( ( IsModifierKeyDown() ) and ( NuNSettings[local_player.realmName] ) ) then
		if ( ( ( not NuNSettings[local_player.realmName].mouseBttn ) and ( mBttn == "LeftButton" ) ) or ( mBttn == NuNSettings[local_player.realmName].mouseBttn ) ) then
			if ( not NuNSettings[local_player.realmName].modKeys ) then
				NuNSettings[local_player.realmName].modKeys = "Alt";
			end
			if ( not NuNSettings[local_player.realmName].antiKey ) then
				NuNSettings[local_player.realmName].antiKey = "Control-Shift";
			end

			local modKeys = {};
			if ( strfind(NuNSettings[local_player.realmName].modKeys, "-") ) then
				modKeys = { strsplit("-", NuNSettings[local_player.realmName].modKeys) };			-- Note my curly brackets....
			else
				modKeys[1] = NuNSettings[local_player.realmName].modKeys;
			end
			local totalKeys = getn(modKeys);
			local keysPressed = 0;
			for i=1, totalKeys, 1 do
				local keyFunc = _G["Is"..modKeys[i].."KeyDown"];
				if ( ( type(keyFunc) == "function" ) and ( keyFunc() ) ) then	-- I think LUA has short circuit logic
					keysPressed = keysPressed + 1;
				end
			end

			-- STRICT ENFORCEMENT :
			-- The following makes sure that <Control><Alt><Shift> would be ignored where the Modifier Set is <Control><Shift> alone....
			--  i.e. it would allow the full Set of <Control><Alt><Shift> to be used by another AddOn without invoking NotesUNeeds limited <Control><Shift> Set
			local antiKey = {};
			if ( strfind(NuNSettings[local_player.realmName].antiKey, "-") ) then
				antiKey = { strsplit("-", NuNSettings[local_player.realmName].antiKey) };			-- Note my curly brackets....
			else
				antiKey[1] = NuNSettings[local_player.realmName].antiKey;
			end
			local totalAntiKey = getn(antiKey);
			local antiKeyPressed = 0;
			for i=1, totalAntiKey, 1 do
				local keyFunc = _G["Is"..antiKey[i].."KeyDown"];
				if ( ( type(keyFunc) == "function" ) and ( keyFunc() ) ) then	-- I think LUA has short circuit logic
					antiKeyPressed = antiKeyPressed + 1;
				end
			end

			if ( ( keysPressed == totalKeys ) and ( antiKeyPressed == 0 ) ) then
				return true;
			end
		end
	end
	return nil;
end


-----------------------------------------------------------------------------------------------


function NuN_ImportSocialNotes_Start()
	if ( not SocialNotes ) then
		NuN_Message("SocialNotes AddOn NOT Detected !");
	else
		local loCount = 0;
		for __, playerDetails in pairs(SocialNotes) do
			if ( ( playerDetails.title ) and ( playerDetails.body ) ) then
				loCount = loCount + 1;
				sn.Array[ loCount ] = {};
				sn.Array[ loCount ].title = playerDetails.title;
				sn.Array[ loCount ].body = playerDetails.body;
			end
		end
		sn.totCount = loCount;
	end
end

function NuN_ImportSocialNote_Terminate()
	NuN_Message(sn.Imported .. " Social Notes Imported in to NuN from a total of " .. sn.totCount);
	if ( sn.Exists > 0 ) then NuN_Message(sn.Exists .. " Notes already Imported"); end
	if ( sn.Ignored > 0 ) then NuN_Message(sn.Ignored .. " Social Notes Skipped"); end
	sn.Waiting = nil;
	sn.totCount = 0;
	sn.count = 0;
	sn.Imported, sn.Ignored = 0, 0;
	sn.Array = {};
end		

-- Import Social Notes
function NuN_ProcessSocialNote(playerDetails)
	sn.Name = playerDetails.title;
	sn.Body = playerDetails.body;
				
	if ( locals.NuNDataPlayers[sn.Name] ) then
		local tstTxt = NuNF.NuN_GetCText(sn.Name);
		if ( not strfind(tstTxt, NUN_SN_FLAG) ) then
			local tmpTxt = contact.text;
			contact.text = tstTxt .. "\n" .. sn.Body .. "\n" .. NUN_SN_FLAG;
			NuNF.NuN_SetCText(sn.Name);
			contact.text = tmpTxt;
			sn.Imported = sn.Imported + 1;
		else
			sn.Exists = sn.Exists + 1;
		end

	elseif ( sn.CancelAll ) then
		sn.Ignored = sn.Ignored + 1;

	else
		StaticPopup_Show("NUN_CHOOSE_SN_IMPORT");
	end

end


-----------------------------------------------------------------------------------------------

function NuN_StripColorCode(txt)
	if txt and type(txt) == "string" then
		txt = strgsub(txt, "(\124c%x%x%x%x%x%x%x%x)(.-)(\124r)", "%2")
	end	
	return txt;
end

function NuN_ColourText(noteType, fBttn, mBttn)
	local eBox = NuNGNoteTextScroll;
	
	if ( noteType == "General" ) then
	
	elseif ( noteType == "Contact" ) then
		eBox = NuNText;
	end

	-- Toggle display of colour markers or change Preset colour selection
	if ( mBttn == "RightButton" ) then

		-- Open Colour Picker to change a preset if necessary
		if ( fBttn.preset ) then
			-- Reset the preset if Alt key is down
			if ( IsAltKeyDown() ) then
				local hexVal = NuNC.NUN_CPRESETS[ fBttn:GetID() ];
				local cKey = "cc";
				if ( fBttn.parentType == "General" ) then
					cKey = "gc";
				end
				cKey = cKey .. fBttn:GetID();
				NuNSettings[local_player.realmName][cKey] = hexVal;
				fBttn.preset = hexVal;
				
				local r, g, b = NuNF.NuN_HtoD(hexVal);
				_G[fBttn:GetName().."Texture"]:SetVertexColor(r, g, b);


			-- or open the colour picker to choose a different preset colour
			else
				if ( ColorPickerFrame:IsVisible() ) then
					if ( ColorPickerFrame.cancelFunc ) then
						ColorPickerFrame.cancelFunc(ColorPickerFrame.previousValues);
					end
					HideUIPanel(ColorPickerFrame);
				else
					NuNF.NuN_ChoosePresetColour(fBttn);
				end
			end

		-- else Right Click on Colour Picker button to Toggle display of Colour Tags in the Edit Box
		else
			if ( fBttn.showCTags ) then
				NuN_AllowColours(fBttn.parentType);
				local dText = eBox:GetText();
				dText = strgsub(dText, "\124\124", "|");
				-- converting double to single is safe in one step
				dText = strgsub(dText, "|C", "|c");
				dText = strgsub(dText, "|R", "|r");
				dText = strgsub(dText, "||c", "|c");
				dText = strgsub(dText, "||r", "|r");
				eBox:SetText(dText);

			else
				NuN_ForbidColours(fBttn.parentType);
				local dText = eBox:GetText();
				dText = strgsub(dText, "\124\124", "|");
				-- convert ALL double to single FIRST, otherwise double could be converted to triple by the end...
				dText = strgsub(dText, "|C", "|c");
				dText = strgsub(dText, "|R", "|r");
				dText = strgsub(dText, "||c", "|c");
				dText = strgsub(dText, "||r", "|r");
				-- the actual conversion we want
				dText = strgsub(dText, "|c", "||c");
				dText = strgsub(dText, "|r", "||r");
				eBox:SetText(dText);
			end
		end


	-- Choose custom colour selection for text
	elseif ( mBttn == "LeftButton" ) then
		-- if a preset Colour Button, then apply the preset colour
		if ( fBttn.preset ) then
			NuNF.NuN_ColourPicked(eBox, fBttn.preset);


		-- else open the colour picker to choose a custom colour....
		else
			-- need a way of testing full acceptance of a colour
			-- I can NOT apply the colour EVERY time it changes, unless I reselect the text (i.e. rehighlight it...)
			-- the best option would be to store the colour change EVERY time, but I need to hook when the Actual OK button is clicked...
			-- Gonna set up the Color Picker with a dummy .func() and monitor the Okay button instead
			if ( not fBttn.showCTags ) then
				if ( ColorPickerFrame:IsVisible() ) then
					if ( ColorPickerFrame.cancelFunc ) then
						ColorPickerFrame.cancelFunc(ColorPickerFrame.previousValues);
					end
					HideUIPanel(ColorPickerFrame);
				else
					NuNF.NuN_ChooseTextColour(eBox, textToColour);
				end
			end
		end
	end

end


-- Functions to choose custom text colours
function NuNF.NuN_ChooseTextColour(eBox, textToColour)
	local col = { r=1, g=1, b=1 };
	if ( NuNSettings[local_player.realmName].lastCustomC ) then
		col.r = NuNSettings[local_player.realmName].lastCustomC.r;
		col.g = NuNSettings[local_player.realmName].lastCustomC.g;
		col.b = NuNSettings[local_player.realmName].lastCustomC.b;
	end

	ColorPickerFrame.eBox = eBox;
	ColorPickerFrame.textToColour = textToColour;
	ColorPickerFrame.hasOpacity = false;
	ColorPickerFrame.func = NuNF.NuN_AcceptTextColour;
	ColorPickerFrame.cancelFunc = NuNF.NuN_CancelTextColour;
	ColorPickerFrame.previousValues = {col.r, col.g, col.b};
	ColorPickerFrame:SetFrameStrata("FULLSCREEN_DIALOG");
	ColorPickerFrame.opacity = 1.0;
	ColorPickerFrame:SetColorRGB(col.r, col.g, col.b);
	NuN_ColorPickerOkayMask:Show();				-- Intercept actual "accept the colour" clicks on the Okay button
	ColorPickerFrame:Show();
end

function NuNF.NuN_AcceptTextColour()
	-- do nothing : only execute when Okay clicked
end

function NuNF.NuN_CancelTextColour(col)
	-- do nothing : only do something when Okay clicked
	NuN_ColorPickerOkayMask:Hide();
end

function NuN_ColorPickerOkay()
	local r, g, b = ColorPickerFrame:GetColorRGB();
	NuNSettings[local_player.realmName].lastCustomC = {};
	NuNSettings[local_player.realmName].lastCustomC.r = r;
	NuNSettings[local_player.realmName].lastCustomC.g = g;
	NuNSettings[local_player.realmName].lastCustomC.b = b;
	NuNF.NuN_ColourPicked(ColorPickerFrame.eBox, NuNF.NuN_DtoH(r, g, b));
	NuN_ColorPickerOkayMask:Hide();
end


-- Function to apply chosen colour to text *!*
function NuNF.NuN_ColourPicked(eBox, toColour)
	local textToColour = NuNF.NuN_GetSelectedText(eBox);

	if ( textToColour ) then
		-- Very basic colouring technique
--		textToColour = toColour .. textToColour .. "|r";
		-- More advanced colouring technique
		textToColour = NuNF.NuN_Colouriser(textToColour, toColour);
		eBox:Insert(textToColour);	
	end
end

-- Try my best to ensure all selected text that is not already "colourised" is NOW colourised...
-- I can't guarantee it, as I don't exhaustively look for colour tags that are "in effect" BEFORE/AFTER the highlighted region... and I ain't going to either ;p This is pretty good for requirements
function NuNF.NuN_Colouriser(textToColour, toColour)
	local tF, tT, qTst = 0;
	local colouredText = "";
	local nextString;
	local openTag = false;
	local col;
	local txtLen = strlen(textToColour);

	textToColour = strgsub(textToColour, "\124\124", "|");
	textToColour = strgsub(textToColour, "|C", "|c");
	textToColour = strgsub(textToColour, "|R", "|r");

	-- if wanting to replace the colouring of selected text that includes its own tag start and end, then do in one fell swoop...
	if ( ( strsub(textToColour, 1, 2) == "|c" ) and ( strsub(textToColour, txtLen-1, txtLen) == "|r" ) ) then
		colouredText = toColour .. strsub(textToColour, 11);
		return colouredText;
	end

	-- otherwise, iterate through the strlooking for 'clean' segments of text to colour in...
	while ( true ) do
		tT = strfind(textToColour, "|", tF+1);

		if ( not tT ) then
			nextString = strsub(textToColour, tF+1);
			if ( ( nextString ) and ( nextString ~= "" ) ) then
				if ( openTag ) then
					col = "";
				else
					col = toColour;
				end
				colouredText = colouredText .. col .. nextString .. "|r";
				openTag = false;
			end
			break;
		else
			qTst = strsub(textToColour, tT, tT+1);
			if ( qTst == "|r" ) then
				nextString = strsub(textToColour, tF+1, tT+1);
				if ( ( nextString ) and ( nextString ~= "" ) ) then
					colouredText = colouredText .. strsub(textToColour, tF+1, tT+1);
				end
				tF = tT + 1;

			elseif ( qTst == "|c" ) then
				nextString = strsub(textToColour, tF+1, tT-1);
				if ( ( nextString ) and ( nextString ~= "" ) ) then				
					if ( openTag ) then
						col = "";
					else
						col = toColour;
					end
					colouredText = colouredText .. col .. nextString .. "|r";
					openTag = false;
				end
				tF = tT - 1;
				tT = strfind(textToColour, "|r", tF+1);
				if ( not tT ) then
					colouredText = colouredText .. strsub(textToColour, tF+1);
					break;
				else
					colouredText = colouredText .. strsub(textToColour, tF+1, tT+1);
					tF = tT + 1;
				end

			else
				nextString = strsub(textToColour, tF+1, tT);
				if ( openTag ) then
					col = "";
				else
					col = toColour;
				end				
				colouredText = colouredText .. col .. nextString;
				openTag = true;
				tF = tT;
			end
		end
	end

	-- final tidy up of any opening tags that we inserted, and have not already closed...
	if ( openTag ) then colouredText = colouredText .. "|r"; end
	
	return colouredText;
end

-- Functions to select custom Presets
function NuNF.NuN_ChoosePresetColour(fBttn)
	local col = {};
	col.r, col.g, col.b = NuNF.NuN_HtoD(fBttn.preset);

	ColorPickerFrame.fBttn = fBttn;
	ColorPickerFrame.hasOpacity = false;
	ColorPickerFrame.func = NuNF.NuN_AcceptPresetColour;
	ColorPickerFrame.cancelFunc = NuNF.NuN_CancelPresetColour;
	ColorPickerFrame.previousValues = {col.r, col.g, col.b};
	ColorPickerFrame:SetFrameStrata("FULLSCREEN_DIALOG");
	ColorPickerFrame.opacity = 1.0;
	ColorPickerFrame:SetColorRGB(col.r, col.g, col.b);
	ColorPickerFrame:Show();
end

function NuNF.NuN_AcceptPresetColour()
	local r, g, b = ColorPickerFrame:GetColorRGB();
	NuNF.NuN_ApplyPresetColour(ColorPickerFrame.fBttn, r, g, b);
end

function NuNF.NuN_CancelPresetColour(col)
	if ( col ) then
		NuNF.NuN_ApplyPresetColour(ColorPickerFrame.fBttn, col[1], col[2], col[3]);
		ColorPickerFrame.fBttn = nil;
	end
end

function NuNF.NuN_ApplyPresetColour(fBttn, r, g, b)
	local hexVal = NuNF.NuN_DtoH(r, g, b);
	local cKey = "cc";
	if ( fBttn.parentType == "General" ) then
		cKey = "gc";
	end
	cKey = cKey .. fBttn:GetID();

	NuNSettings[local_player.realmName][cKey] = hexVal;
	fBttn.preset = hexVal;

	_G[fBttn:GetName().."Texture"]:SetVertexColor(r, g, b);
end


function NuN_AllowColours(fType)
	if ( fType == "Contact" ) then
		NuNCColourButton.showCTags = nil;
		NuNCColourPreset1:Show();
		NuNCColourPreset2:Show();
		NuNCColourPreset3:Show();
		NuNCColourPreset4:Show();
		NuNCColourPreset5:Show();

	elseif ( fType == "General" ) then
		NuNGColourButton.showCTags = nil;
		NuNGColourPreset1:Show();
		NuNGColourPreset2:Show();
		NuNGColourPreset3:Show();
		NuNGColourPreset4:Show();
		NuNGColourPreset5:Show();
	end
end

function NuN_ForbidColours(fType)
	if ( fType == "Contact" ) then
		NuNCColourButton.showCTags = true;
		NuNCColourPreset1:Hide();
		NuNCColourPreset2:Hide();
		NuNCColourPreset3:Hide();
		NuNCColourPreset4:Hide();
		NuNCColourPreset5:Hide();

	elseif ( fType == "General" ) then
		NuNGColourButton.showCTags = true;
		NuNGColourPreset1:Hide();
		NuNGColourPreset2:Hide();
		NuNGColourPreset3:Hide();
		NuNGColourPreset4:Hide();
		NuNGColourPreset5:Hide();
	end
end


----------------------------------------------------------------------------------------------



function NuN_ChatDelay_EditBoxInit()
	NuN_ChatDelay_EditBox:SetText( NuN_DTrans.tDelay );
end


function NuN_ChatDelay_EditBoxValidate()
	local delay = NuN_ChatDelay_EditBox:GetText();
	delay = tonumber(delay);

	if ( not delay ) then
		NuN_ChatDelay_EditBoxInit();
		return true;

	else
		NuNSettings[local_player.realmName].delay = delay;
		NuN_DTrans.tDelay = delay;
	end
end




-- Editing short cut keys...
function NuN_ShortCut(possibleToSave, key)
	if ( ( possibleToSave ) and ( possibleToSave:IsEnabled() ) ) then
		if ( key == "S" ) then
			if ( IsAltKeyDown ) then
				local func = possibleToSave:GetScript("OnClick");
				if ( func ) then
					NuN_SaveReport = true;
					func();
					NuN_SaveReport = nil;
				end
			end
		end
	end
end

function NuN_ShortCut_OnUpdate(eFrame, eBox)
	if ( IsAltKeyDown() ) then
		if ( eFrame.hasFocus ) then
			eFrame.saveReady = eBox:GetCursorPosition();
			eBox:ClearFocus();
		end
		eFrame:EnableKeyboard(true);

	else
		if ( not eFrame.hasFocus ) then
			if ( eFrame.saveReady ) then
				eBox:SetFocus( eFrame.saveReady );
				eFrame.saveReady = nil;
			end
		end
		eFrame:EnableKeyboard(false);
	end
end


-----------------------------------------------------------------------------------------------

-- LOCALISATION RELATED FUNCTIONS

-----------------------------------------------------------------------------------------------


function NuN_LangPatch(langDirection, single)
	local toDeutschAR = {
		4,
		1,
		2,
		3
	}

	local toDeutschHC = {
		1,
		3,
		5,
		6,
		8,
		7,
		2,
		4
	}

	local toDeutschAC = {
		1,
		3,
		5,
		6,
		7,
		8,
		2,
		4
	}

	local toDeutschP = {
		1,
		12,
		13,
		14,
		15,
		16,
		18,
		3,
		4,
		5,
		6,
		8,
		9,
		10,
		11,
		2,
		7,
		17
	}
	local idx, value;

	if ( locals.NuNDataPlayers ) then
		if ( langDirection == "->de" ) then
			for idx, value in pairs(locals.NuNDataPlayers) do
				if ( locals.NuNDataPlayers[idx].faction ) then
					if ( ( not single ) or ( ( single ) and ( idx == single ) ) ) then
						if ( locals.NuNDataPlayers[idx].faction == "Horde" ) then
							if ( locals.NuNDataPlayers[idx].cls ) then
								locals.NuNDataPlayers[idx].cls = toDeutschHC[locals.NuNDataPlayers[idx].cls];
							end
						else
							if ( locals.NuNDataPlayers[idx].race ) then
								locals.NuNDataPlayers[idx].race = toDeutschAR[locals.NuNDataPlayers[idx].race];
							end
							if ( locals.NuNDataPlayers[idx].cls ) then
								locals.NuNDataPlayers[idx].cls = toDeutschAC[locals.NuNDataPlayers[idx].cls];
							end
						end
						if ( locals.NuNDataPlayers[idx].prof1 ) then
							locals.NuNDataPlayers[idx].prof1 = toDeutschP[locals.NuNDataPlayers[idx].prof1];
						end
						if ( locals.NuNDataPlayers[idx].prof2 ) then
							locals.NuNDataPlayers[idx].prof2 = toDeutschP[locals.NuNDataPlayers[idx].prof2];
						end
					end
				end
			end

		elseif ( langDirection == "->en" ) then
			for idx, value in pairs(locals.NuNDataPlayers) do
				if ( locals.NuNDataPlayers[idx].faction ) then
					if ( ( not single ) or ( ( single ) and ( idx == single ) ) ) then
						if ( locals.NuNDataPlayers[idx].faction == "Horde" ) then
							if ( locals.NuNDataPlayers[idx].cls ) then
								locals.NuNDataPlayers[idx].cls = NuNF.NuNGet_TableID(toDeutschHC, locals.NuNDataPlayers[idx].cls);
							end
						else
							if ( locals.NuNDataPlayers[idx].race ) then
								locals.NuNDataPlayers[idx].race = NuNF.NuNGet_TableID(toDeutschAR, locals.NuNDataPlayers[idx].race);
							end
							if ( locals.NuNDataPlayers[idx].cls ) then
								locals.NuNDataPlayers[idx].cls = NuNF.NuNGet_TableID(toDeutschAC, locals.NuNDataPlayers[idx].cls);
							end
						end
						if ( locals.NuNDataPlayers[idx].prof1 ) then
							locals.NuNDataPlayers[idx].prof1 = NuNF.NuNGet_TableID(toDeutschP, locals.NuNDataPlayers[idx].prof1);
						end
						if ( locals.NuNDataPlayers[idx].prof2 ) then
							locals.NuNDataPlayers[idx].prof2 = NuNF.NuNGet_TableID(toDeutschP, locals.NuNDataPlayers[idx].prof2);
						end
					end
				end
			end
		end
	end
end

-----------------------------------------------------------------------------------------------



-----------------------------------------------------------------------------------------------
-- Friend/Ignore Management Hooks and Related Functions
-----------------------------------------------------------------------------------------------
-- Helper function
function NuN_Is_Ignored(aName)
	local iName;
	for i = 1, GetNumIgnores(), 1 do
		iName = GetIgnoreName(i);
		if ( iName == aName ) then
			return true;
		end
	end
	return false;
end
-- Helper function
function NuN_Is_Friendly(aName)
	local iName;
	for i = 1, GetNumFriends(), 1 do
		iName = GetFriendInfo(i);
		if ( iName == aName ) then
			return true;
		end
	end
	return false;
end



function NuNNew_AddFriend(...)
	local _name = select(1, ...);

	if ( type(_name) == "number" ) then
		_name = GetFriendInfo(_name);

	elseif ( ( _name ) and ( _name == "target" ) ) then
		_name = UnitName(_name);

	elseif ( ( type(_name) == "string" ) and ( _name == "" ) ) then
		_name = UnitName("target");
	end

	if ( not locals.NuN_FriendIgnoreActivity ) then
		if ( _name ) then
			locals.NuN_FriendUpdate.func = NuN_AddFriend;
			locals.NuN_FriendUpdate.name = _name;
			locals.NuN_FriendUpdate.time = GetTime();
		else
			locals.NuN_FriendUpdate.func = nil;
			locals.NuN_FriendUpdate.name = nil;
			locals.NuN_FriendUpdate.time = 0;
		end
	end
end

function NuN_AddFriend(_name)
	if ( ( _name ~= nil ) and ( _name ~= UNKNOWN ) and ( _name ~= UNKNOWNOBJECT ) and ( NuN_Is_Friendly(_name) ) ) then -- 5.60
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not NuNSettings[local_player.realmName].autoS ) ) then
			NuN_Message(FRIENDS.." "..name);
		end
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not locals.NuNDataPlayers[_name] ) ) then
			locals.NuNDataPlayers[_name] = {};
			locals.NuNDataPlayers[_name].type = NuNC.NUN_AUTO_C;
			locals.NuNDataPlayers[_name].faction = local_player.factionName;
			locals.NuNDataPlayers[_name][locals.txtTxt] = NUN_AUTO_FRIEND..NuNF.NuN_GetDateStamp();
		end
		if ( locals.NuNDataPlayers[_name] ) then
			locals.NuNDataPlayers[_name].friendLst = true;
		end
		if ( NuNSettings[local_player.realmName].gNotFriends[_name] ) then
			NuNSettings[local_player.realmName].gNotFriends[_name] = nil;
		end
		locals.NuN_FriendUpdate.func = nil;
		locals.NuN_FriendUpdate.name = nil;
		locals.NuN_FriendUpdate.time = 0;
	end
	NuNNew_FriendsList_Update();
end



function NuNNew_RemoveFriend(...)
	local _name = select(1, ...);
	if ( type(_name) == "number" ) then
		_name = GetFriendInfo(_name);
	end

	if ( not locals.NuN_FriendIgnoreActivity ) then
		if ( _name ) then
			locals.NuN_FriendUpdate.func = NuN_RemoveFriend;
			locals.NuN_FriendUpdate.name = _name;
			locals.NuN_FriendUpdate.time = GetTime();
		else
			locals.NuN_FriendUpdate.func = nil;
			locals.NuN_FriendUpdate.name = nil;
			locals.NuN_FriendUpdate.time = 0;
		end
	end
end

function NuN_RemoveFriend(_name)
	if ( ( _name ~= nil ) and ( _name ~= UNKNOWN ) and ( _name ~= UNKNOWNOBJECT ) and ( not NuN_Is_Friendly(_name) ) ) then -- 5.60
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not NuNSettings[local_player.realmName].autoS ) ) then
			NuN_Message(DELETE.." "..FRIENDS.." "..name);
		end
		if ( locals.NuNDataPlayers[_name] ) then
			if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[_name].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[_name].ignoreLst ) ) then
				locals.NuNDataPlayers[_name] = nil;
			else
				locals.NuNDataPlayers[_name].friendLst = nil;
			end
		end
		if ( not NuNSettings[local_player.realmName].gNotFriends[_name] ) then
			NuNSettings[local_player.realmName].gNotFriends[_name] = true;
		end
		locals.NuN_FriendUpdate.func = nil;
		locals.NuN_FriendUpdate.name = nil;
		locals.NuN_FriendUpdate.time = 0;
	end
	NuNNew_FriendsList_Update();
end



function NuNNew_AddIgnore(...)
	local _name = select(1, ...);

	if ( type(_name) == "number" ) then
		_name = GetIgnoreName(_name);
	end

	if ( not locals.NuN_FriendIgnoreActivity ) then
		if ( _name ) then
			locals.NuN_IgnoreUpdate.func = NuN_AddIgnore;
			locals.NuN_IgnoreUpdate.name = _name;
			locals.NuN_IgnoreUpdate.time = GetTime();
		else
			locals.NuN_IgnoreUpdate.func = nil;
			locals.NuN_IgnoreUpdate.name = nil;
			locals.NuN_IgnoreUpdate.time = 0;
		end
	end
end

function NuN_AddIgnore(_name)
	if ( ( _name ~= nil ) and ( _name ~= UNKNOWN ) and ( _name ~= UNKNOWNOBJECT ) and ( NuN_Is_Ignored(_name) ) ) then -- 5.60
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not NuNSettings[local_player.realmName].autoS ) ) then
			NuN_Message(IGNORE.." "..name);
		end
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not locals.NuNDataPlayers[_name] ) ) then
			locals.NuNDataPlayers[_name] = {};
			locals.NuNDataPlayers[_name].type = NuNC.NUN_AUTO_C;
			locals.NuNDataPlayers[_name].faction = local_player.factionName;
			locals.NuNDataPlayers[_name][locals.txtTxt] = NUN_AUTO_IGNORE..NuNF.NuN_GetDateStamp();
		end
		if ( locals.NuNDataPlayers[_name] ) then
			locals.NuNDataPlayers[_name].ignoreLst = true;
		end
		if ( NuNSettings[local_player.realmName].gNotIgnores[_name] ) then
			NuNSettings[local_player.realmName].gNotIgnores[_name] = nil;
		end
		locals.NuN_IgnoreUpdate.func = nil;
		locals.NuN_IgnoreUpdate.name = nil;
		locals.NuN_IgnoreUpdate.time = 0;
	end
	NuNNew_IgnoreList_Update();
end



function NuNNew_DelIgnore(...)
	local _name = select(1, ...);

	if ( type(_name) == "number" ) then
		_name = GetIgnoreName(_name);

	elseif ( ( _name ) and ( _name == "target" ) ) then
		_name = UnitName(_name);

	elseif ( ( type(_name) == "string" ) and ( _name == "" ) ) then
		_name = UnitName("target");
	end

	if ( not locals.NuN_FriendIgnoreActivity ) then
		if ( _name ) then
			locals.NuN_IgnoreUpdate.func = NuN_DelIgnore;
			locals.NuN_IgnoreUpdate.name = _name;
			locals.NuN_IgnoreUpdate.time = GetTime();
		else
			locals.NuN_IgnoreUpdate.func = nil;
			locals.NuN_IgnoreUpdate.name = nil;
			locals.NuN_IgnoreUpdate.time = 0;
		end
	end
end

function NuN_DelIgnore(_name)
	if ( ( _name ~= nil ) and ( _name ~= UNKNOWN ) and ( _name ~= UNKNOWNOBJECT ) and ( not NuN_Is_Ignored(_name) ) ) then -- 5.60
		if ( ( NuNSettings[local_player.realmName].autoFI ) and ( not NuNSettings[local_player.realmName].autoS ) ) then
			NuN_Message(DELETE.." "..IGNORE.." "..name);
		end
		if ( locals.NuNDataPlayers[_name] ) then
			if ( ( NuNSettings[local_player.realmName].autoD ) and ( locals.NuNDataPlayers[_name].type == NuNC.NUN_AUTO_C ) and ( not locals.NuNDataPlayers[_name].friendLst ) ) then
				locals.NuNDataPlayers[_name] = nil;
			else
				locals.NuNDataPlayers[_name].ignoreLst = nil;
			end
		end
		if ( not NuNSettings[local_player.realmName].gNotIgnores[_name] ) then
			NuNSettings[local_player.realmName].gNotIgnores[_name] = true;
		end
		locals.NuN_IgnoreUpdate.func = nil;
		locals.NuN_IgnoreUpdate.name = nil;
		locals.NuN_IgnoreUpdate.time = 0;
	end
	NuNNew_IgnoreList_Update();
end



function NuNNew_AddOrDelIgnore(...)
	local _name = select(1, ...);

	if ( type(_name) == "number" ) then
		_name = GetIgnoreName(_name);

	elseif ( ( _name ) and ( _name == "target" ) ) then
		_name = UnitName(_name);

	elseif ( ( type(_name) == "string" ) and ( _name == "" ) ) then
		_name = UnitName("target");
	end

	if ( not locals.NuN_FriendIgnoreActivity ) then
		if ( _name ) then
			if ( NuN_Is_Ignored(_name) ) then
				locals.NuN_IgnoreUpdate.func = NuN_DelIgnore;
				locals.NuN_IgnoreUpdate.name = _name;
				locals.NuN_IgnoreUpdate.time = GetTime();
			else
				locals.NuN_IgnoreUpdate.func = NuN_AddIgnore;
				locals.NuN_IgnoreUpdate.name = _name;
				locals.NuN_IgnoreUpdate.time = GetTime();
			end
		else
			locals.NuN_IgnoreUpdate.func = nil;
			locals.NuN_IgnoreUpdate.name = nil;
			locals.NuN_IgnoreUpdate.time = 0;
		end
	end
end

-- Friend/Ignore Maintenance Hooks and related functions
-----------------------------------------------------------------------------------------------

--------------------------------------------------------------------------------------------------------------------------------------
-- TOOLTIPS
--
-- Retain special Tooltip display function
--

--
-- Replace ALL the following functions with a default Tooltip Handler
--
--function NuN_CustomFontCheckBox_OnEnter()
--function NuN_FFButton_OnEnter()
--function NuN_OptionsButton_OnEnter()
--function NuN_BrowseButton_OnEnter()
--function NuN_ContactButton_OnEnter()
--function NuN_GNoteButton_OnEnter()
--function NuN_ResetButton_OnEnter()
--function NuN_LocButton_OnEnter()
--function NuN_GNoteTitle_OnEnter()
--function NuN_SaveDefCheck_OnEnter()
--function NuN_RestoreDefButton_OnEnter()
--function NuN_TargetButton_OnEnter()
--function NuN_NPCTargetButton_OnEnter()
--function NuN_WhoButton_OnEnter()
--function NuN_UserButtons_OnEnter(owner)
--function NuN_ClearDD_OnEnter()
--function NuNMapNoteButton_OnEnter()
--function NuN_PinnedTT_OnEnter()
--function NuN_OpenChat_OnEnter()
--
-- ??? .fromQuest or NOT .fromQuest, that is the question ??? --
--function NuN_GNoteButtonDelete_OnEnter()
--
--function NuN_TTCheckBox_OnEnter()
--
--function NuN_ChatCheckBox_OnEnter()
--function NuN_ChatFormatCheckBox_OnEnter()
--
--function NuN_Level_CheckBox_OnEnter()
--function NuNGTypeDropDown_OnEnter()
--function NuNScaleFrameButton_OnEnter()
--
--function NuN_PartyDownButton_OnEnter()
--function NuN_bHaveTTCheckBox_OnEnter()
--function NuN_Options_Maintain_OnEnter()
--function NuN_Options_AA_OnEnter()
--function NuN_Options_Verbose_OnEnter()
--
--function NuN_AutoMapCheckBox_OnEnter()
-- 5.60
--function NuN_GuildRefreshCheckBox_OnEnter()
--function NuN_GRVerboseCheckBox_OnEnter()
--function NuN_ModifierMasterCheckBox_OnEnter()
--function NuN_Modifier_OnEnter()
--function NuN_HyperButton_OnEnter()
--
--function NuN_RunButton_OnEnter()
--

function NuN_DisplayTooltip(self, ttBase, shouldWrap)
	if ( NuNSettings[local_player.realmName].toolTips ) then
		if shouldWrap then shouldWrap = 1 end
		NuN_State.NuN_Fade = false;
		local x, y = GetCursorPosition();
		if ( x > 500 ) then
			NuN_Tooltip:SetOwner(self, "ANCHOR_TOPRIGHT");
		else
			NuN_Tooltip:SetOwner(self, "ANCHOR_TOPLEFT");
		end
		NuN_Tooltip:ClearLines();
		local ttKey = ttBase .. 1;
--NuN_Message("self:" .. tostring(self) .. "    ttKey:" .. tostring(ttKey) .. "   NuNC[ttKey]:" .. NuNC[ttKey]);
		if ( NuNC[ttKey] ) then
			NuN_Tooltip:AddLine(NuNC[ttKey], 1, 0.7, 0);
			for i=2, 9, 1 do
				ttKey = ttBase .. i;
				if ( NuNC[ttKey] ) then
					NuN_Tooltip:AddLine(NuNC[ttKey], 0, 1, 0, shouldWrap);			
				else
					break;
				end
			end
			NuN_Tooltip:Show();
		end
	end
end

-- Talents Tooltip
function NuN_TalentsTooltip(self)
	local talents;

	if ( ( locals.NuNDataPlayers[local_player.currentNote.unit] ) and ( locals.NuNDataPlayers[local_player.currentNote.unit].talents ) ) then
		talents = locals.NuNDataPlayers[local_player.currentNote.unit].talents;
	
	elseif ( ( NuNTalents.player ) and ( NuNTalents.player == local_player.currentNote.unit ) ) then
		talents = NuNTalents;
		
	else
		return;
	end

	if ( talents.total > 0 ) then
		NuN_State.NuN_Fade = false;
		local x, y = GetCursorPosition();
		if ( x > 500 ) then
			NuN_Tooltip:SetOwner(self, "ANCHOR_TOPRIGHT");
		else
			NuN_Tooltip:SetOwner(self, "ANCHOR_TOPLEFT");
		end
		NuN_Tooltip:ClearLines();

		local details, colorL, colorR = nil, NORMAL_FONT_COLOR, GREEN_FONT_COLOR;
		local insertSpacer = nil;
		
		-- Could set predominent icon here ... tDets.icon
		NuN_Tooltip:AddLine(talents.summary, 0, 0, 1);
		for _tab = 1, talents.tabs do
			if ( talents[_tab] ) then
				specifics = talents[_tab].specifics;
				if ( getn(specifics) > 0 ) then
					if insertSpacer then NuN_Tooltip:AddLine("\n"); else insertSpacer = true; end
					NuN_Tooltip:AddDoubleLine(talents[_tab].spec, talents[_tab].points, 1, 1, 1, 1, 1, 1);
					NuN_Tooltip:AddTexture(talents[_tab].icon);
					details = "";
					tsort(specifics, NuNF.NuN_SortTalentArray);
					for talentIdx = 1, getn(specifics) do
						local talentDetails = specifics[talentIdx];
						if ( ( talentDetails.curR ) and ( talentDetails.curR > 0 ) ) then
							colorR = GREEN_FONT_COLOR;
							if ( talentDetails.curR < talentDetails.maxR ) then
								colorR = RED_FONT_COLOR;
							end
							NuN_Tooltip:AddDoubleLine(talentDetails.talentName, "(" .. talentDetails.curR .. "/" .. (talentDetails.maxR or "1") .. ")", colorL.r, colorL.g, colorL.b, colorR.r, colorR.g, colorR.b);
						end
					end
					NuN_Tooltip:AddLine(details);
				end
			end
		end
		NuN_Tooltip:Show();

	end
end

--NuNC.NUN_TT_AUTOP_OFF NuNSettings[local_player.realmName].autoP
function NuN_PartyInfo_OnEnter(self)
	if ( not NuNSettings[local_player.realmName].autoP ) then
		NuN_DisplayTooltip(self,"NUN_TT_AUTOP_OFF");
		return;
	end

	local tipArray = {};
	local tipLines = 0;

	for myAlt, record in pairs(locals.NuNDataPlayers[local_player.currentNote.unit]) do
		if ( ( type(record) == "table" ) and ( record.partied ) and ( record.partiedOn ) ) then
			tipLines = tipLines + 1;
			tipArray[ tipLines ] = {};
			tipArray[ tipLines ].left = myAlt .. " |cffff0000(x" .. record.partied .. ")|r";
			tipArray[ tipLines ].right = record.partiedOn;
		end
	end

	NuN_State.NuN_Fade = false;
	local x, y = GetCursorPosition();
	if ( x > 500 ) then
		NuN_Tooltip:SetOwner(self, "ANCHOR_TOPRIGHT");
	else
		NuN_Tooltip:SetOwner(self, "ANCHOR_TOPLEFT");
	end
	NuN_Tooltip:ClearLines();

	NuN_Tooltip:AddLine(NUN_PARTIED_WITH, 1, 0.7, 0);
	for line, details in ipairs(tipArray) do
		NuN_Tooltip:AddDoubleLine(details.left, details.right, 0, 1, 0, 0.1, 0.1, 0.9);
	end

	NuN_Tooltip:Show();
end

-- TOOLTIPS
--------------------------------------------------------------------------------------------------------------------------------------


--------------------------------------------------------------------------------------------------------------------------------------
-- External functions for other AddOns to fetch / save text in NotesUNeed notes

-- simple function for checking for existance of General notes
function NuN_GNoteExists(tstNote, simpleCheck)
	local cmplxName;

	if ( ( NuNDataRNotes[tstNote] ) or ( NuNDataANotes[tstNote] ) ) then
		if ( not simpleCheck ) then
			local_player.currentNote.general = tstNote;
		end
		return true;

	elseif ( NuNData[locals.itmIndex_dbKey][tstNote] ) then
		cmplxName = NuNData[locals.itmIndex_dbKey][tstNote];
		if ( ( NuNDataRNotes[cmplxName] ) or ( NuNDataANotes[cmplxName] ) ) then
			if ( not simpleCheck ) then
				local_player.currentNote.general = cmplxName;
			end
			return true;
		end
	end

	return false;
end

-- Arguments to pass :
--  - noteType = "General" / "Contact"
--  - noteName = [name of the note EXACTLY]
-- If an error occurs, and the returned text is nil, then the second Return Parameter can be examined to determine the problem
--  1	"Invalid noteType"
--  2	"General Note not Found"
--  3	"Contact Note nof Found"
function NuN_ReturnText(noteType, noteName)
	if ( noteType == "General" ) then
		if ( ( NuNDataRNotes[gLclNote] ) or ( NuNDataANotes[gLclNote] ) ) then
			return NuNF.NuN_GetGText(gLclNote);
		end
		return nil, 2;

	elseif ( noteType == "Contact" ) then
		if ( locals.NuNDataPlayers[noteName] ) then
			return NuNF.NuN_GetCText(noteName);
		end
		return nil, 3;
	end
	
	return nil, 1;
end

-- Arguments to pass :
--  - noteType = "General" / "Contact"
--  - noteName = name of the note EXACTLY
--  - noteText = the text to be inserted
--  - pos = specifies how and where the text is to be inserted
--	-1	:	Replace the entire text
--	#	:	a number specififying the character position after which to insert the text
--			0 = beginning, 1 = after first character, etc.
--			(will append the note if target text is not long enough, but will ALSO return error code 5)
--	<nil>	:	omit to append the text at the end of the note (i.e. default behaviour)
--
-- NOTE : its the responsibility of the caller to ensure that there are appropriate carriage returns, spearaators, etc.
--
-- Function does not return anything when succesful
-- If an error occurs, then the second Return Parameter can be examined to determine the problem
-- 1 "No text passed"
-- 2 "Invalid noteType"
-- 3 "General Note not Found"
-- 4 "Contact Note not Found"
-- 5 "Text appended to note because specified insert position not found"	(*non-critical error)
function NuN_InsertText(noteType, noteName, noteText, pos)
	local errorCode = nil;
	
	if ( ( not noteText ) or ( noteText == "" ) ) then
		return 1;
	end
	if ( ( pos ) and ( type(pos) ~= "number" ) ) then
		pos = nil;
	end
	
	if ( noteType == "General" ) then
		if ( ( NuNDataRNotes[noteName] ) or ( NuNDataANotes[noteName] ) ) then

			local nLvl = "Account";
			if ( NuNDataRNotes[noteName] ) then
				nLvl = "Realm";
			end
			local fetchedText = NuNF.NuN_GetGText(noteName);
			if ( ( not pos ) or ( pos > strlen(fetchedText) ) ) then
				if ( pos > strlen(fetchedText) ) then
					errorCode = 5;
				end
				fetchedText = fetchedText .. noteText;

			elseif ( pos < 0 ) then
				fetchedText = noteText;

			else
				fetchedText = strsub(fetchedText, 1, pos) .. noteText .. strsub(fetchedText, pos+1);
			end

			if ( ( NuNGNoteFrame:IsVisible() ) and ( local_player.currentNote.general == noteName ) ) then
				NuNGNoteTextBox:SetText( fetchedText );
			end

			local tmpN = local_player.currentNote.general;
			local tmpT = general.text;
			local_player.currentNote.general = noteName;
			general.text = fetchedText;
			NuNF.NuN_SetCText(nLvl);
			general.text = tmpT;
			local_player.currentNote.general = tmpN;
			return errorCode;

		end
		return 3;

	elseif ( noteType == "Contact" ) then
		if ( locals.NuNDataPlayers[noteName] ) then

			local fetchedText = NuNF.NuN_GetCText(noteName);
			if ( ( not pos ) or ( pos > strlen(fetchedText) ) ) then
				if ( ( pos ) and ( pos > strlen(fetchedText) ) ) then
					errorCode = 5;
				end
				fetchedText = fetchedText .. noteText;

			elseif ( ( pos ) and ( pos < 0 ) ) then
				fetchedText = noteText;

			else
				fetchedText = strsub(fetchedText, 1, pos) .. noteText .. strsub(fetchedText, pos+1);				
			end
			
			if ( ( NuNFrame:IsVisible() ) and ( local_player.currentNote.unit == noteName ) ) then
				NuNText:SetText(fetchedText);
			end
			
			local tmpT = contact.text;
			contact.text = fetchedText;
			NuNF.NuN_SetCText(noteName);
			contact.text = tmpT;
			return errorCode;

		end
		return 4;

	end	
	return 2;
end


-- External functions for other AddOns to fetch / save text in NotesUNeed notes
--------------------------------------------------------------------------------------------------------------------------------------



--[[
Scratchpad:

GetGuildFactionGroup() == 0 (Horde) else (Alliance)



--]]