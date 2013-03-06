function doChangeCall(id) {
    jQuery.post("/laifeditor/ajax/elemchange", {
        "id" : id, 
        "content" : document.getElementById(id).innerHTML
    }).complete(function() {
        $("#"+id+"_result").load("/laifeditor/ajax/evalrule", {"id" : id});
    });
} 


function confirmSave() {
    $("#confirm_save_dialog").dialog({
        "modal":true
    });
}

function closeSaveDialog() {
    $("#confirm_save_dialog").dialog('close');
}

// Lexica

function closeLexicon(id) {
    jQuery.post("/laifeditor/ajax/removelexicon", {
        "id" : id
    }, function(response, status, xhr) {
        if (status == "error") {
            var msg = "Sorry but there was an error: ";
            alert(msg + xhr.status + " " + xhr.statusText);
        }
    });
    $("#"+id).remove()
}

function addLexicon() {
    $("#add_lexicon_dialog").dialog({
        "modal":true, 
        "width":650
    })
}

function doAddLexicon() {
    var uri = $("#add_lexicon_dialog_lexiconURI").val();
    $("#add_lexicon_dialog").dialog('close');
    $.post("/laifeditor/ajax/addLexicon", {
        "uri":uri
    },function(data) {
        $("#lexicatable").append(data)
    });
}


// Functional Rules

function deleteFuncRule(id) {
    jQuery.post("/laifeditor/ajax/removerule", {
        "id" : id
    }, function(response, status, xhr) {
        if (status == "error") {
            var msg = "Sorry but there was an error: ";
            alert(msg + xhr.status + " " + xhr.statusText);
        }
    });
    $("#"+id+"_1").remove()
    $("#"+id+"_2").remove()
}

function addFuncRule() {
    $("#add_funcrule_dialog").dialog({
        "modal":true, 
        "width":650
    })
}

function doAddFuncRule() {
    var key = $("#add_funcrule_dialog_key").val();
    var argcount = $("#add_funcrule_dialog_argcount").val();
    $("#add_funcrule_dialog").dialog('close');
    $.post("/laifeditor/ajax/addrule", {
        "key":key,
        "argcount":argcount
    },function(data) {
        $("#funcruletable").append(data)
    });
}


// Non-functional rules

function deleteNonFuncRule(id) {
    jQuery.post("/laifeditor/ajax/removerule", {
        "id" : id
    }, function(response, status, xhr) {
        if (status == "error") {
            var msg = "Sorry but there was an error: ";
            alert(msg + xhr.status + " " + xhr.statusText);
        }
    });
    $("#"+id+"_1").remove()
    $("#"+id+"_2").remove()
    $("#"+id+"_3").remove()
}

function addNonFuncRule() {
    $("#add_nonfuncrule_dialog").dialog({
        "modal":true, 
        "width":650
    })
}

function doAddNonFuncRule() {
    var key = $("#add_nonfuncrule_dialog_key").val();
    $("#add_nonfuncrule_dialog").dialog('close');
    $.post("/laifeditor/ajax/addrule", {
        "key":key
    },function(data) {
        $("#nonfuncruletable").append(data)
    });
}

var currentRule = null;

function addUserCallDialog(id) {
    currentRule = id;
    $("#add_usercall_select").load("/laifeditor/ajax/getfuncrules",function() {
        $("#add_usercall_dialog").dialog({"modal":true});
    });
}

function doAddUserCall() {
    alert($("#add_usercall_select option:selected").val())
    $("#add_usercall_dialog").dialog('close');
    $("#"+currentRule+"_editable").load("/laifeditor/ajax/addusercall", {
        "id" : currentRule,
        "call" : $("#add_usercall_select  option:selected").val()
    }, function() {
        doChangeCall(currentRule);
    });
}


function addFormCallDialog(id) {
    currentRule = id;
    $("#add_formcall_select").load("/laifeditor/ajax/getforms",function() {
        $("#add_formcall_dialog").dialog({"modal":true, "width": 650});
    });
}

function doAddFormCall() {
    alert($("#add_formcall_select option:selected").val())
    $("#add_formcall_dialog").dialog('close');
    $("#"+currentRule+"_editable").load("/laifeditor/ajax/addformcall", {
        "id" : currentRule,
        "call" : $("#add_formcall_select  option:selected").val()
    }, function() {
        doChangeCall(currentRule);
    });
}