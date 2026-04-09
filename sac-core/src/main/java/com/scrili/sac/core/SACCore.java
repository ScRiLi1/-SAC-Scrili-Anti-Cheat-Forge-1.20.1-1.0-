package com.scrili.sac.core;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.checks.*;
import com.scrili.sac.core.log.LogManager;
import com.scrili.sac.core.punishment.PunishmentManager;

public class SACCore {

    private final ISACConfig config;
    private final PunishmentManager punishmentManager;
    private final SpeedCheck speedCheck;
    private final FlyCheck flyCheck;
    private final ModBlacklistCheck modBlacklistCheck;
    private final ReachCheck reachCheck;
    private final JesusCheck jesusCheck;
    private final FastAttackCheck fastAttackCheck;
    private final ScaffoldCheck scaffoldCheck;
    private final AutoClickerCheck autoClickerCheck;
    private final KillAuraCheck killAuraCheck;
    private final AimAssistCheck aimAssistCheck;
    private final FreecamCheck freecamCheck;
    private final MouseAimCheck mouseAimCheck;

    public SACCore(ISACConfig config) {
        LogManager.init();
        this.config             = config;
        this.punishmentManager  = new PunishmentManager(config);
        this.speedCheck         = new SpeedCheck(config, punishmentManager);
        this.flyCheck           = new FlyCheck(config, punishmentManager);
        this.modBlacklistCheck  = new ModBlacklistCheck(config, punishmentManager);
        this.reachCheck         = new ReachCheck(config, punishmentManager);
        this.jesusCheck         = new JesusCheck(punishmentManager);
        this.fastAttackCheck    = new FastAttackCheck(config, punishmentManager);
        this.scaffoldCheck      = new ScaffoldCheck(config, punishmentManager);
        this.autoClickerCheck   = new AutoClickerCheck(config, punishmentManager);
        this.killAuraCheck      = new KillAuraCheck(config, punishmentManager);
        this.aimAssistCheck     = new AimAssistCheck(punishmentManager);
        this.freecamCheck       = new FreecamCheck(config, punishmentManager);
        this.mouseAimCheck      = new MouseAimCheck(punishmentManager);
    }

    public ISACConfig getConfig()                    { return config; }
    public PunishmentManager getPunishmentManager()  { return punishmentManager; }
    public SpeedCheck getSpeedCheck()                { return speedCheck; }
    public FlyCheck getFlyCheck()                    { return flyCheck; }
    public ModBlacklistCheck getModBlacklistCheck()  { return modBlacklistCheck; }
    public ReachCheck getReachCheck()                { return reachCheck; }
    public JesusCheck getJesusCheck()                { return jesusCheck; }
    public FastAttackCheck getFastAttackCheck()      { return fastAttackCheck; }
    public ScaffoldCheck getScaffoldCheck()          { return scaffoldCheck; }
    public AutoClickerCheck getAutoClickerCheck()    { return autoClickerCheck; }
    public KillAuraCheck getKillAuraCheck()          { return killAuraCheck; }
    public AimAssistCheck getAimAssistCheck()        { return aimAssistCheck; }
    public FreecamCheck getFreecamCheck()            { return freecamCheck; }
    public MouseAimCheck getMouseAimCheck()          { return mouseAimCheck; }
}
