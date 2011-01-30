function admin_do( pl, handler, id, encoded, decoded )

	local players = player.GetAll()
	local ply = players[ decoded[ 2 ] ]
	local act = decoded[ 1 ]

	if act == "slap" then
		ply:SetHealth( ply:Health() - 1 )
		if ply:Health() == 0 then
			ply:Kill()
		end
		AddNotify( ply, "You've been slapped by an admin!", 1, 5 )
	elseif act == "slay" then
		ply:Kill()
		AddNotify( ply, "You've been slayed by an admin!", 1, 5 )
	elseif act == "jail" then
		JailPlayer( ply )
		AddNotify( ply, "You've been arrested by an admin!", 1, 5 )
	elseif act == "free" then
		FreePlayer( ply )
		AddNotify( ply, "You've been freed by an admin!", 1, 5 )
	elseif act == "freeze" then
		ply:Freeze( true )
		AddNotify( ply, "You've been frozen by an admin!", 1, 5 )
	elseif act == "unfreeze" then
		ply:Freeze( false )
		AddNotify( ply, "You've been unfrozen by an admin!", 1, 5 )
	elseif act == "strip" then
		ply:StripWeapons()
		ply:StripAmmo()
		ply:Give( "krp_hands" )
		AddNotify( ply, "You've been stripped of weapons by an admin!", 1, 5 )
	elseif act == "kick" then
		ply:Kick( "Bye." )
	elseif act == "temp" then
		ply:Ban( 2880, "Please fuck off for a couple days." )
		ply:Kick( "Bye." )
	elseif act == "ban" then
		ply:Ban( 0, "Fuck off." )
		ply:Kick( "Bye." )
	end

end
datastream.Hook( "admin_do", admin_do )
function zombeh_mode( pl, handler, id, encoded, decoded )

	if !zombieMode then
		zombieMode = true
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "ZOMBIE MODE ENABLED!" )
		end
	else
		zombieMode = false
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "ZOMBIE MODE DISABLED! Kill all the remaining rebels!" )
		end
	end
end
datastream.Hook( "zombeh_mode", zombeh_mode )
function rebel_mode( pl, handler, id, encoded, decoded )

	if !rebelMode then
		rebelMode = true
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "REBEL MODE ENABLED!" )
		end
	else
		rebelMode = false
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "REBEL MODE DISABLED! Kill all the remaining rebels!" )
		end
	end
end
datastream.Hook( "rebel_mode", rebel_mode )
function combine_mode( pl, handler, id, encoded, decoded )

	if !combineMode then
		combineMode = true
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "COMBINE MODE ENABLED!" )
		end
	else
		combineMode = false
		for k, v in pairs( player.GetAll() ) do
			v:PrintMessage( HUD_PRINTCENTER, "COMBINE MODE DISABLED! Kill all the remaining combine!" )
		end
	end
end
datastream.Hook( "combine_mode", combine_mode )